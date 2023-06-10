package consumer

import com.github.thake.kafka.avro4k.serializer.KafkaAvro4kDeserializer
import com.github.thake.kafka.avro4k.serializer.KafkaAvro4kDeserializerConfig
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.io.Closeable
import java.time.Duration
import java.util.*


class KafkaConsumerImpl<T>(
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

    fun consume(): Flow<T> {
        return flow {
            consumer.subscribe(listOf(topic))
            while (true) {
                val records = consumer.poll(Duration.ofMillis(100))

                for (record in records) {
                    val value = record.value()
                    emit(value)
                }
                if (!currentCoroutineContext().isActive) {
                    println("Coroutine inactive")
                    break
                }
            }
        }
    }

    override fun close() {
        consumer.close()
    }
}