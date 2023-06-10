import kotlinx.coroutines.runBlocking
import repository.Repository
import repository.RepositoryImpl

fun main(): Unit = runBlocking {
    val repository: Repository = RepositoryImpl(
        jdbcUrl = dbUrl,
        driverClassName = driverClassName,
        username = dbUser,
        password = dbPassword
    )
    val bookCreatedProducer = BookCreatedProducer(kafkaBootstrapServers, schemaRegistryUrl)
    val service: Service = ServiceImpl(repository, bookCreatedProducer)
    service.use {
        val server = Server()
        server.start(serverPort = port, service = it)
    }
}
