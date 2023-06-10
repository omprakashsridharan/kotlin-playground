interface Service {
    suspend fun createBook(title: String, isbn: String): Long
}