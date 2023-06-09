package consumer

import com.github.thake.kafka.avro4k.serializer.KafkaAvro4kDeserializer
import com.github.thake.kafka.avro4k.serializer.KafkaAvro4kDeserializerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.io.Closeable
import java.time.Duration
import java.util.*


class KafkaConsumer<T>(
    bootstrapServers: String,
    schemaRegistryUrl: String,
    groupId: String,
    private val topic: String
) : Closeable {
    private var consumer: KafkaConsumer<String, T>

    init {
        val consumerProps = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaAvro4kDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvro4kDeserializer::class.java)
            put(KafkaAvro4kDeserializerConfig.RECORD_PACKAGES, "*")
            put("schema.registry.url", schemaRegistryUrl)
        }
        consumer = KafkaConsumer(consumerProps)
    }

    suspend fun consume(onMessageReceived: (T) -> Unit): Flow<T> {
        return flow {
            consumer.subscribe(listOf(topic))
            consumer.asFlow().collect { record ->
                emit(record.value())
            }
        }.flowOn(Dispatchers.IO)
            .buffer()
            .onEach { message ->
                onMessageReceived(message)
            }
            .catch { exception ->
                // Handle any errors that occur during consumption
                println("Error during message consumption: $exception")
            }
    }


    override fun close() {
        consumer.close()
    }
}

fun <K, V> KafkaConsumer<K, V>.asFlow(timeout: Duration = Duration.ofMillis(500)): Flow<ConsumerRecord<K, V>> =
    flow {
        poll(timeout).forEach { emit(it) }
    }