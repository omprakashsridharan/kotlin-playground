package producer

class BookCreatedProducer(bootstrapServers: String, schemaRegistryUrl: String) : Producer {
    private var kafkaProducer: KafkaProducerImpl<CreatedBook> = KafkaProducerImpl(bootstrapServers, schemaRegistryUrl)
    override suspend fun publishCreatedBook(createdBook: CreatedBook): Boolean =
        this.kafkaProducer.produce(
            Topics.BOOK_CREATED.name,
            createdBook.id,
            createdBook
        )
}