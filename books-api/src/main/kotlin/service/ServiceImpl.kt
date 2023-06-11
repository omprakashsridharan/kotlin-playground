package service

import BookCreatedProducer
import common.messaging.dto.CreatedBook
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import repository.Repository
import tracing.trace

class ServiceImpl(
    private val repository: Repository,
    private val bookCreatedProducer: BookCreatedProducer,
    private val tracer: Tracer
) : Service {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java);

    override suspend fun createBook(title: String, isbn: String): Result<Long> {

        try {
            return tracer.trace("createBookService") {
                val createdBookResult = repository.createBook(title, isbn)
                if (createdBookResult.isSuccess) {
                    val createdBookId = createdBookResult.getOrNull()
                    CoroutineScope(Dispatchers.IO).launch {
                        val result =
                            bookCreatedProducer.publishCreatedBook(CreatedBook(createdBookId.toString(), title, isbn))
                        logger.info("Book created publish result $result")
                    }
                    it.setStatus(StatusCode.OK)
                    it.setAttribute(
                        "service.create.book.result",
                        createdBookResult.getOrThrow().toString()
                    )
                } else {
                    it.setStatus(StatusCode.ERROR)
                    it.setAttribute(
                        "service.create.book.error",
                        createdBookResult.exceptionOrNull().toString()
                    )
                }
                return@trace createdBookResult
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
