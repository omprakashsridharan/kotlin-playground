import kotlinx.coroutines.runBlocking
import repository.Repository
import repository.RepositoryImpl
import service.Service
import service.ServiceImpl

fun main(): Unit = runBlocking {
    val openTelemetry = tracing.initializeOpenTelemetry(zipkinEndpoint, "books-api")
    val tracerProvider = openTelemetry.tracerProvider
    val tracer = tracerProvider.get("com.omprakash.kotlin-playground.books-api");

    val repository: Repository = RepositoryImpl(
        jdbcUrl = dbUrl,
        driverClassName = driverClassName,
        username = dbUser,
        password = dbPassword,
        tracer = tracer
    )
    val bookCreatedProducer = BookCreatedProducer(kafkaBootstrapServers, schemaRegistryUrl, tracer)
    val service: Service = ServiceImpl(repository, bookCreatedProducer, tracer)

    val server = Server(service = service, openTelemetry = openTelemetry)
    server.start(serverPort = port)
}
