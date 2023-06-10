package request.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateBookRequest(val title: String, val isbn: String)