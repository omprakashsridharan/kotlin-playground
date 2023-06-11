import io.opentelemetry.api.trace.Span
import kotlinx.coroutines.runBlocking
import repository.Repository
import repository.RepositoryImpl

fun main(): Unit = runBlocking {
    val openTelemetry = tracing.initializeOpenTelemetry(zipkinEndpoint, "books-api")
    val tracerProvider = openTelemetry.tracerProvider
    val tracer = tracerProvider.get("com.omprakash.kotlin-playground");
    val span: Span = tracer.spanBuilder("Start my wonderful use case").startSpan()
    try {
        span.makeCurrent().use { scope ->
            span.addEvent("Event 0")
            span.addEvent("Event 1")
        }
    } finally {
        span.end()
    }
    val repository: Repository = RepositoryImpl(
        jdbcUrl = dbUrl,
        driverClassName = driverClassName,
        username = dbUser,
        password = dbPassword
    )
    val bookCreatedProducer = BookCreatedProducer(kafkaBootstrapServers, schemaRegistryUrl)
    val service: Service = ServiceImpl(repository, bookCreatedProducer)

    val server = Server()
    server.start(serverPort = port, service = service)
}
