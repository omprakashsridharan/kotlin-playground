package producer

import kotlinx.serialization.Serializable


@Serializable
data class CreatedBook(val id: String, val title: String, val isbn: String)

interface Producer {
    suspend fun publishCreatedBook(createdBook: CreatedBook): Boolean
}