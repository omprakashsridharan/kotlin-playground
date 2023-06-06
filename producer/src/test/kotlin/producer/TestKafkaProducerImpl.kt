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
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.util.*

@Testcontainers
class TestKafkaProducerImpl {
    companion object {

        @Container
        val kafkaContainer: KafkaContainer =
            KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0")).withKraft()
        private lateinit var bookCreatedProducerImpl: KafkaProducerImpl

        @JvmStatic
        @BeforeAll
        fun startContainers(): Unit {
            kafkaContainer.start()
        }
    }

    @BeforeEach
    fun setUp() {
        bookCreatedProducerImpl = KafkaProducerImpl(
            kafkaContainer.bootstrapServers,
            "mock://"
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