package producer

import com.github.thake.kafka.avro4k.serializer.KafkaAvro4kDeserializer
import com.github.thake.kafka.avro4k.serializer.KafkaAvro4kDeserializerConfig
import common.messaging.constants.Topic
import common.messaging.dto.CreatedBook
import kotlinx.coroutines.test.runTest
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
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
class TestKafkaProducer {
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


        private lateinit var bookCreatedProducerImpl: KafkaProducerImpl<CreatedBook>
        private lateinit var schemaRegistryUrl: String

        @JvmStatic
        @BeforeAll
        fun startContainers() {
            val startables = listOf(kafkaContainer, schemaRegistryContainer)
            Startables.deepStart(startables)
        }
    }

    @BeforeEach
    fun setUp() {
        schemaRegistryUrl = "http://${schemaRegistryContainer.host}:${schemaRegistryContainer.getMappedPort(8081)}"
        bookCreatedProducerImpl = KafkaProducerImpl<CreatedBook>(
            kafkaContainer.bootstrapServers,
            schemaRegistryUrl
        )
    }

    @Test
    fun testSchemaChecking() = runTest {
        val topic = "string-topic"
        val stringProducerImpl1 = KafkaProducerImpl<String>(
            kafkaContainer.bootstrapServers,
            schemaRegistryUrl
        )
        val publishResult1 = stringProducerImpl1.produce(topic, "TEST", "TEST")
        assertTrue(publishResult1)
        val stringProducerImpl2 = KafkaProducerImpl<CreatedBook>(
            kafkaContainer.bootstrapServers,
            schemaRegistryUrl
        )
        val createdBook = CreatedBook("1", "B1", "ISBN1")
        val publishResult2 = stringProducerImpl2.produce(topic, "TEST", createdBook)
        assertFalse(publishResult2)
    }

    @Test
    fun testPublishCreatedBook() = runTest {
        val topic = Topic.BOOK_CREATED.name
        val createdBook = CreatedBook("1", "B1", "ISBN1")
        val bookCreatedProducer = KafkaProducerImpl<CreatedBook>(
            kafkaContainer.bootstrapServers,
            schemaRegistryUrl
        )
        val publishResult = bookCreatedProducer.produce(Topic.BOOK_CREATED.name, createdBook.id, createdBook)
        assertTrue(publishResult)
        val consumerProps = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer")
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaAvro4kDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvro4kDeserializer::class.java)
            put(KafkaAvro4kDeserializerConfig.RECORD_PACKAGES, "*")
            put("schema.registry.url", schemaRegistryUrl)
        }

        KafkaConsumer<String, CreatedBook>(consumerProps).use { consumer ->
            consumer.subscribe(listOf(topic))
            val records = consumer.poll(Duration.ofSeconds(5))
            assertEquals(1, records.count())
            val record = records.first()
            assertEquals(createdBook.id, record.key())
            assertEquals(createdBook, record.value())
        }
    }
}