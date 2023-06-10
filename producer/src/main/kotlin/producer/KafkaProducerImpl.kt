package producer

import com.github.thake.kafka.avro4k.serializer.KafkaAvro4kSerializer
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import org.apache.kafka.clients.producer.*
import java.time.Instant
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class KafkaProducerImpl<T>(bootstrapServers: String, schemaRegistryUrl: String) {

    private var producer: KafkaProducer<String, T>

    init {
        val producerProps = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to KafkaAvro4kSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaAvro4kSerializer::class.java,
            AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl,
            "security.protocol" to "PLAINTEXT"
        )
        producer = KafkaProducer(producerProps)
    }

    suspend fun produce(topic: String, key: String, value: T): Boolean {
        val result = runCatching {
            producer.asyncSend(ProducerRecord(topic, null, Instant.now().toEpochMilli(), key, value))
        }
        return if (result.isSuccess) {
            println("Successfully produced message to topic $topic, metadata: ${result.getOrNull().toString()}")
            true
        } else {
            println("Error while producing message to topic $topic, error: ${result.exceptionOrNull()}")
            false
        }
    }

}


suspend fun <K, V> Producer<K, V>.asyncSend(record: ProducerRecord<K, V>) =
    suspendCoroutine<RecordMetadata> { continuation ->
        this.send(record) { metadata, exception ->
            exception?.let(continuation::resumeWithException) ?: continuation.resume(metadata)
        }
    }