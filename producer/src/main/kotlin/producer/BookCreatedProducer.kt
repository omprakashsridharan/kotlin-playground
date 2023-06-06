package producer

class BookCreatedProducer(private val kafkaProducer: KafkaProducerImpl<CreatedBook>) : Producer {
    override suspend fun publishCreatedBook(createdBook: CreatedBook): Boolean =
        this.kafkaProducer.produce(
            Topics.BOOK_CREATED.name,
            createdBook.id,
            createdBook
        )
}