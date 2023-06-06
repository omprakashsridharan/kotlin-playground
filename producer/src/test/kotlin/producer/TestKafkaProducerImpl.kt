package producer

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.lifecycle.Startables
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.util.*

@Testcontainers
class TestKafkaProducerImpl {
    companion object {
        val network = Network.newNetwork()

        @Container
        val kafkaContainer: KafkaContainer =
            KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0")).withKraft().withNetwork(network)
                .waitingFor(Wait.forLogMessage(".*KafkaServer.*started.*", 1))

        @Container
        val schemaRegistryContainer: GenericContainer<*> =
            GenericContainer(DockerImageName.parse("confluentinc/cp-schema-registry:7.4.0"))
                .withNetwork(network)
                .dependsOn(kafkaContainer)
                .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:9092")
                .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                .withExposedPorts(8081)


        private lateinit var bookCreatedProducerImpl: KafkaProducerImpl

        @JvmStatic
        @BeforeAll
        fun startContainers(): Unit {
            val startables = listOf(kafkaContainer, schemaRegistryContainer)
            Startables.deepStart(startables)
        }
    }

    @BeforeEach
    fun setUp() {
        bookCreatedProducerImpl = KafkaProducerImpl(
            kafkaContainer.bootstrapServers,
            "http://${schemaRegistryContainer.host}:${schemaRegistryContainer.getMappedPort(8081)}"
        )
    }

    @AfterEach
    fun tearDown() {
        bookCreatedProducerImpl.close()
    }

    @Test
    fun testPublishCreatedBook() = runTest {
        val topic = Topics.BOOK_CREATED.name
        val createdBook = CreatedBook("1", "B1", "ISBN1")
        val bookCreatedProducer = BookCreatedProducer(bookCreatedProducerImpl)
        bookCreatedProducer.publishCreatedBook(createdBook)

        val consumerProps = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer")
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer::class.java)
            put("schema.registry.url", "mock://")
        }

        KafkaConsumer<String, ByteArray>(consumerProps).use { consumer ->
            consumer.subscribe(listOf(topic))
            val records = consumer.poll(Duration.ofSeconds(5))
            assertEquals(1, records.count())
            val record = records.first()
            assertEquals(createdBook.id, record.key())
            assertEquals(createdBook, Json.decodeFromString<CreatedBook>(String(record.value())))
        }
    }
}