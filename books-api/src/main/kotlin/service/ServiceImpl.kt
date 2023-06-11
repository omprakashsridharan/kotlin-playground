package service

import BookCreatedProducer
import common.messaging.dto.CreatedBook
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import repository.Repository

class ServiceImpl(
    private val repository: Repository,
    private val bookCreatedProducer: BookCreatedProducer,
    private val tracer: Tracer
) : Service {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java);

    override suspend fun createBook(title: String, isbn: String): Result<Long> {
        val createBookServiceSpan =
            tracer.spanBuilder("createBookService").setSpanKind(SpanKind.INTERNAL)
                .setParent(Context.current().with(Span.current()))
                .startSpan()
        try {
            return withContext(createBookServiceSpan.asContextElement()) {
                val createdBookResult = repository.createBook(title, isbn)
                if (createdBookResult.isSuccess) {
                    val createdBookId = createdBookResult.getOrNull()
                    CoroutineScope(Dispatchers.IO).launch {
                        val result =
                            bookCreatedProducer.publishCreatedBook(CreatedBook(createdBookId.toString(), title, isbn))

                        logger.info("Book created publish result $result")
                    }
                    createBookServiceSpan.setStatus(StatusCode.OK)
                    createBookServiceSpan.setAttribute(
                        "service.create.book.result",
                        createdBookResult.getOrThrow().toString()
                    )
                } else {
                    createBookServiceSpan.setStatus(StatusCode.ERROR)
                    createBookServiceSpan.setAttribute(
                        "service.create.book.error",
                        createdBookResult.exceptionOrNull().toString()
                    )
                }
                return@withContext createdBookResult
            }

        } catch (e: Exception) {
            return Result.failure(e)
        } finally {
            createBookServiceSpan.end()
        }
    }
}
