interface Service : AutoCloseable {
    suspend fun createBook(title: String, isbn: String): Long
}