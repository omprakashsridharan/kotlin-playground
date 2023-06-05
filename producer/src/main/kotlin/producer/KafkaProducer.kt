package producer

import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.StringSerializer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

enum class Topics {
    BOOK_CREATED
}

class KafkaProducerImpl(private val bootstrapServers: String) : producer.Producer, AutoCloseable {

    private var producer: KafkaProducer<String, Any>

    init {
        val producerProps = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaJsonSchemaSerializer::class.java,
            "security.protocol" to "PLAINTEXT"
        )
        producer = KafkaProducer(producerProps)
    }

    private suspend fun <T> produce(topic: String, key: String, value: T): Boolean =
        producer.use {
            val result = runCatching {
                it.asyncSend(ProducerRecord(topic, key, value))
            }
            if (result.isSuccess) {
                return@use true
            } else {
                println("Error while producing message to topic $topic, error: ${result.exceptionOrNull()}")
                return@use false
            }
        }

    override suspend fun publishCreatedBook(createdBook: CreatedBook): Boolean =
        produce(Topics.BOOK_CREATED.name, createdBook.id.toString(), createdBook)

    override fun close() {
        producer.close()
    }

}

suspend fun <K, V> Producer<K, V>.asyncSend(record: ProducerRecord<K, V>) =
    suspendCoroutine<RecordMetadata> { continuation ->
        this.send(record) { metadata, exception ->
            exception?.let(continuation::resumeWithException) ?: continuation.resume(metadata)
        }
    }