import database.Repository
import server.Service

class ServiceImpl(private val repository: Repository) : Service {

    override suspend fun createBook(title: String, isbn: String): Long = repository.createBook(title, isbn).await()

    override fun close() {
        TODO("Not yet implemented")
    }
}