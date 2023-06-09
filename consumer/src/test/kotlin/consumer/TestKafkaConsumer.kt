package consumer

import com.github.thake.kafka.avro4k.serializer.KafkaAvro4kSerializer
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.lifecycle.Startables
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.atomic.AtomicInteger

@Testcontainers
class TestKafkaConsumer {
    companion object {
        val network = Network.newNetwork()

        @Container
        val kafkaContainer: KafkaContainer =
            KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0")).withKraft().withNetwork(network)
                .withNetworkAliases("kafka")
                .withEnv("KAFKA_HOST_NAME", "kafka")
                .waitingFor(Wait.forLogMessage(".*KafkaServer.*started.*", 1))

        @Container
        val schemaRegistryContainer: GenericContainer<*> =
            GenericContainer(DockerImageName.parse("confluentinc/cp-schema-registry:7.4.0"))
                .withNetwork(network)
                .dependsOn(kafkaContainer)
                .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:9092")
                .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                .withExposedPorts(8081)

        private lateinit var schemaRegistryUrl: String

        @JvmStatic
        @BeforeAll
        fun startContainers() {
            val startables = listOf(kafkaContainer, schemaRegistryContainer)
            Startables.deepStart(startables)
        }
    }

    private fun <T> getProducer(): KafkaProducer<String, T> {
        val producerProps = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaContainer.bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to KafkaAvro4kSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaAvro4kSerializer::class.java,
            AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl,
            "security.protocol" to "PLAINTEXT"
        )
        return KafkaProducer<String, T>(producerProps)
    }

    @BeforeEach
    fun setUp() {
        schemaRegistryUrl = "http://${schemaRegistryContainer.host}:${schemaRegistryContainer.getMappedPort(8081)}"
    }

    @Test
    fun testConsumer() = runBlocking {
        val numberProducer = getProducer<Int>()
        val numberConsumer: KafkaConsumer<Int> =
            KafkaConsumer(kafkaContainer.bootstrapServers, schemaRegistryUrl, "number-group")
        val topic = "number"
        val n = 5
        val numberMessages = (1..n).toList()
        val counter = AtomicInteger()

        val collectJob = launch(Dispatchers.IO) {
            val numberFlow = numberConsumer.consume(topic)
            numberFlow
                .take(n)
                .collect { message ->
                    assertEquals(counter.incrementAndGet(), message)
                }
        }
        collectJob.start()

        for (num in numberMessages) {
            assertDoesNotThrow {
                numberProducer.send(ProducerRecord(topic, num.toString(), num))
            }
            delay(200)
        }
        collectJob.cancel()
        numberConsumer.close()
        assertEquals(n, 5)
    }
}