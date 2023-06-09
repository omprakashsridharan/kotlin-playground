import common.messaging.constants.Topic
import common.messaging.dto.CreatedBook
import producer.KafkaProducerImpl
import producer.Producer

class BookCreatedProducer(bootstrapServers: String, schemaRegistryUrl: String) : Producer {
    private var kafkaProducer: KafkaProducerImpl<CreatedBook> = KafkaProducerImpl(bootstrapServers, schemaRegistryUrl)
    override suspend fun publishCreatedBook(createdBook: CreatedBook): Boolean =
        this.kafkaProducer.produce(
            Topic.BOOK_CREATED.name,
            createdBook.id,
            createdBook
        )
}