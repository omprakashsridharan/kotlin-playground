package response.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateBookResponse(val id: Long, val title: String, val isbn: String)