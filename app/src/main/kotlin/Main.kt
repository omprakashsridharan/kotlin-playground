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
import org.ktorm.database.Database
import org.ktorm.support.postgresql.insertReturning

data class CreateBookRequest(val title: String, val isbn: String)
data class CreateBookResponse(val id: Long, val title: String, val isbn: String)

val scope = CoroutineScope(Dispatchers.Default)

fun main(): Unit = runBlocking {
    val resources = Resources()
    resources.use {
        val routes = getRoutes(resources = it)
        val server = createServer(serverRoutes = routes, port = port)
        scope.launch {
            server.start()
        }
        waitForShutdownSignal()
        println("Shutting down")
        server.stop()
    }
}

suspend fun createServer(serverRoutes: List<ContractRoute>, port: Port): Http4kServer {
    val globalFilters = DebuggingFilters.PrintRequestAndResponse().then(ServerFilters.CatchLensFailure)

    val contract = contract {
        renderer = OpenApi3(ApiInfo("Book publish API", "v1.0"), Jackson)
        descriptionPath = "/openapi.json"
        routes += serverRoutes
    }
    return globalFilters.then(
        routes(
            "/api" bind contract
        )
    ).asServer(Jetty(port.value))
}

suspend fun getRoutes(resources: Resources): List<ContractRoute> {
    val createBooksRoute = createBooksRoute(resources.database)
    return listOf(createBooksRoute)
}

suspend fun waitForShutdownSignal() {
    val deferred = CompletableDeferred<Unit>()
    Runtime.getRuntime().addShutdownHook(Thread {
        deferred.complete(Unit)
    })
    deferred.await()
}

suspend fun createBooksRoute(database: Database): ContractRoute {
    val requestBodyLens = Body.auto<CreateBookRequest>().toLens()
    val responseBodyLens = Body.auto<CreateBookResponse>().toLens()
    val spec = "/books" meta {
        summary = "Creates a new book"
        receiving(requestBodyLens to CreateBookRequest("title", "isbn"))
        returning(Status.OK, responseBodyLens to CreateBookResponse(1L, "title", "isbn"))
    } bindContract Method.POST

    val createBook: HttpHandler = { request ->
        val received: CreateBookRequest = requestBodyLens(request)
        val insertedId = database.insertReturning(Tables.Books, Tables.Books.id) {
            set(it.isbn, received.isbn)
            set(it.title, received.title)
        }
        insertedId?.let {
            val response = CreateBookResponse(it, received.title, received.isbn)
            return@let Response(Status.OK).with(responseBodyLens of response)
        } ?: Response(Status.INTERNAL_SERVER_ERROR)

    }
    return spec to createBook
}
