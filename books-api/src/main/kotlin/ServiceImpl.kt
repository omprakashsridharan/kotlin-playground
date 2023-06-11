import common.messaging.dto.CreatedBook
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import repository.Repository

class ServiceImpl(
    private val repository: Repository,
    private val bookCreatedProducer: BookCreatedProducer,
    private val tracer: Tracer
) : Service {

    override suspend fun createBook(title: String, isbn: String): Long {
        val createBookServiceSpan =
            tracer.spanBuilder("createBookService").setSpanKind(SpanKind.INTERNAL)
                .setParent(Context.current())
                .startSpan()
        val createdBookId = repository.createBook(title, isbn).await()
        val result = bookCreatedProducer.publishCreatedBook(CreatedBook(createdBookId.toString(), title, isbn))
        createBookServiceSpan.setAttribute("publish.result", result)
        println("Book created publish result $result")
        createBookServiceSpan.end()
        return createdBookId
    }
}
