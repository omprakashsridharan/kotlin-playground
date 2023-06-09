package producer

import common.messaging.dto.CreatedBook


interface Producer {
    suspend fun publishCreatedBook(createdBook: CreatedBook): Boolean
}