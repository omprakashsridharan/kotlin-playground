package repository

interface Repository {
    suspend fun createBook(title: String, isbn: String): Result<Long>
}