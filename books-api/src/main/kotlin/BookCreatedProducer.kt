import common.messaging.constants.Topic
import common.messaging.dto.CreatedBook
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import producer.KafkaProducerImpl

class BookCreatedProducer(bootstrapServers: String, schemaRegistryUrl: String, private val tracer: Tracer) {
    private var kafkaProducer: KafkaProducerImpl<CreatedBook> = KafkaProducerImpl(bootstrapServers, schemaRegistryUrl)
    suspend fun publishCreatedBook(createdBook: CreatedBook): Boolean {
        val publishCreatedBookSpan =
            tracer.spanBuilder("publishCreatedBook").setSpanKind(SpanKind.PRODUCER)
                .setParent(Context.current())
                .startSpan()

        val produceResult = this.kafkaProducer.produce(
            Topic.BOOK_CREATED.name,
            createdBook.id,
            createdBook
        )
        if (produceResult) {
            publishCreatedBookSpan.setStatus(StatusCode.OK)
            publishCreatedBookSpan.setAttribute("publish.created.book", true)
        } else {
            publishCreatedBookSpan.setStatus(StatusCode.ERROR)
            publishCreatedBookSpan.setAttribute("publish.created.book", false)
        }
        publishCreatedBookSpan.end()
        return produceResult
    }

}