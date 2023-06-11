import common.messaging.constants.Topic
import common.messaging.dto.CreatedBook
import io.opentelemetry.api.trace.Tracer
import producer.KafkaProducerImpl

class BookCreatedProducer(bootstrapServers: String, schemaRegistryUrl: String, private val tracer: Tracer) {
    private var kafkaProducer: KafkaProducerImpl<CreatedBook> = KafkaProducerImpl(bootstrapServers, schemaRegistryUrl)
    suspend fun publishCreatedBook(createdBook: CreatedBook): Boolean = kafkaProducer.produce(
        Topic.BOOK_CREATED.name,
        createdBook.id,
        createdBook
    )

}