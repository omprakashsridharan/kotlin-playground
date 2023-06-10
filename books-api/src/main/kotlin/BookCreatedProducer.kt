import common.messaging.constants.Topic
import common.messaging.dto.CreatedBook
import producer.KafkaProducerImpl

class BookCreatedProducer(bootstrapServers: String, schemaRegistryUrl: String) {
    private var kafkaProducer: KafkaProducerImpl<CreatedBook> = KafkaProducerImpl(bootstrapServers, schemaRegistryUrl)
    suspend fun publishCreatedBook(createdBook: CreatedBook): Boolean =
        this.kafkaProducer.produce(
            Topic.BOOK_CREATED.name,
            createdBook.id,
            createdBook
        )
}