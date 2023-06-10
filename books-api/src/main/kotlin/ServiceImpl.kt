import common.messaging.dto.CreatedBook
import database.Repository
import server.Service

class ServiceImpl(private val repository: Repository, private val bookCreatedProducer: BookCreatedProducer) : Service {

    override suspend fun createBook(title: String, isbn: String): Long {
        val createdBookId = repository.createBook(title, isbn).await()
        val result = bookCreatedProducer.publishCreatedBook(CreatedBook(createdBookId.toString(), title, isbn))
        println("Book created publish result $result")
        return createdBookId
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}