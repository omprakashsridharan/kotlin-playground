import database.Repository
import database.RepositoryImpl
import kotlinx.coroutines.runBlocking
import server.Server
import server.Service

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
