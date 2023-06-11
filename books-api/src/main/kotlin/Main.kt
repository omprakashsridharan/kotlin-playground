import kotlinx.coroutines.runBlocking
import repository.Repository
import repository.RepositoryImpl

fun main(): Unit = runBlocking {
    val openTelemetry = tracing.initializeOpenTelemetry(zipkinEndpoint, "books-api")
    val tracerProvider = openTelemetry.tracerProvider
    val tracer = tracerProvider.get("com.omprakash.kotlin-playground.books-api");

    val repository: Repository = RepositoryImpl(
        jdbcUrl = dbUrl,
        driverClassName = driverClassName,
        username = dbUser,
        password = dbPassword
    )
    val bookCreatedProducer = BookCreatedProducer(kafkaBootstrapServers, schemaRegistryUrl)
    val service: Service = ServiceImpl(repository, bookCreatedProducer, tracer)

    val server = Server(service = service, openTelemetry = openTelemetry, tracer = tracer)
    server.start(serverPort = port)
}
