package common.messaging.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreatedBook(val id: String, val title: String, val isbn: String)