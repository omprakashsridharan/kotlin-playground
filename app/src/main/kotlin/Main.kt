import database.Repository
import database.RepositoryImpl
import kotlinx.coroutines.*
import server.Server
import server.Service

fun main(): Unit = runBlocking {
    val repository: Repository = RepositoryImpl(
        jdbcUrl = dbUrl,
        driverClassName = driverClassName,
        username = dbUser,
        password = dbPassword
    )
    val service: Service = ServiceImpl(repository)
    service.use {
        val server = Server()
        server.start(serverPort = port, service = it)
    }
}
