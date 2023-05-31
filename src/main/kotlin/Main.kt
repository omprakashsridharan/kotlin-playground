import kotlinx.coroutines.*
import org.http4k.contract.ContractRoute
import org.http4k.contract.contract
import org.http4k.contract.meta
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.core.*
import org.http4k.filter.DebuggingFilters
import org.http4k.filter.ServerFilters
import org.http4k.format.Jackson
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.Jetty
import org.http4k.server.asServer

data class CreateBook(val id: Long, val title: String, val isbn: String)

val scope = CoroutineScope(Dispatchers.Default)

fun main(): Unit = runBlocking {
    val dbConnection = getDatabaseConnection(dbUrl, dbUser, dbPassword)
    dbConnection?.let { connection ->
        val server = createServer()
        scope.launch {
            server.start()
        }
        waitForShutdownSignal()
        println("Shutting down")
        server.stop()
    }
}

suspend fun createServer(): Http4kServer {
    val globalFilters = DebuggingFilters.PrintRequestAndResponse().then(ServerFilters.CatchLensFailure)
    val createBooksRoute = createBooksRoute()
    val contract = contract {
        renderer = OpenApi3(ApiInfo("Book publish API", "v1.0"), Jackson)
        descriptionPath = "/openapi.json"
        routes += createBooksRoute
    }
    return globalFilters.then(
        routes(
            "/api" bind contract
        )
    ).asServer(Jetty(port.value))
}


suspend fun waitForShutdownSignal() {
    val deferred = CompletableDeferred<Unit>()
    Runtime.getRuntime().addShutdownHook(Thread {
        deferred.complete(Unit)
    })
    deferred.await()
}

suspend fun createBooksRoute(): ContractRoute {
    val body = Body.auto<CreateBook>().toLens()
    val spec = "/books" meta {
        summary = "Creates a new book"
        receiving(body to CreateBook(1L, "title", "isbn"))
        returning(Status.OK, body to CreateBook(1L, "title", "isbn"))
    } bindContract Method.POST

    val createBook: HttpHandler = { request ->
        val received: CreateBook = body(request)
        Response(Status.OK).with(body of received)
    }
    return spec to createBook
}
