import common.messaging.constants.Topic
import common.messaging.dto.CreatedBook
import consumer.KafkaConsumerImpl
import kotlinx.coroutines.flow.Flow

class BookCreatedConsumer(bootstrapServers: String, schemaRegistryUrl: String) {
    private var kafkaConsumer: KafkaConsumerImpl<CreatedBook> =
        KafkaConsumerImpl(bootstrapServers, schemaRegistryUrl, "books-created-consumer", Topic.BOOK_CREATED.name)

    fun consumeCreatedBook(): Flow<CreatedBook> = kafkaConsumer.consume()

}