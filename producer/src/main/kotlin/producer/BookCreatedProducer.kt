package producer

import kotlinx.serialization.json.Json


class BookCreatedProducer(private val kafkaProducer: KafkaProducerImpl) : Producer {
    override suspend fun publishCreatedBook(createdBook: CreatedBook): Boolean =
        this.kafkaProducer.produce(
            Topics.BOOK_CREATED.name,
            createdBook.id,
            Json.encodeToString(CreatedBook.serializer(), createdBook).encodeToByteArray()
        )
}