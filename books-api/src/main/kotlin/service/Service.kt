package service

interface Service {
    suspend fun createBook(title: String, isbn: String): Result<Long>
}