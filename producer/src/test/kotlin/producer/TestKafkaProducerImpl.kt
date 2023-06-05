package producer

import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer
import kotlinx.coroutines.test.runTest
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
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
        val kafkaContainer: KafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"))

        private lateinit var producerImpl: KafkaProducerImpl
    }

    @BeforeEach
    fun setUp() {
        producerImpl = KafkaProducerImpl(kafkaContainer.bootstrapServers)
    }

    @AfterEach
    fun tearDown() {
        producerImpl.close()
    }

    @Test
    fun testPublishCreatedBook() = runTest {
        val topic = Topics.BOOK_CREATED.name
        val createdBook = CreatedBook(1L, "B1", "ISBN1")

        producerImpl.publishCreatedBook(createdBook)

        val consumerProps = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer")
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaJsonSchemaDeserializer::class.java)
        }

        KafkaConsumer<String, CreatedBook>(consumerProps).use { consumer ->
            consumer.subscribe(listOf(topic))
            val records = consumer.poll(Duration.ofSeconds(5))
            assertEquals(1, records.count())
            val record = records.first()
            assertEquals(createdBook.id.toString(), record.key())
            assertEquals(createdBook, record.value())
        }
    }
}