import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import request.dto.CreateBookRequest
import response.dto.CreateBookResponse

class Server {
    fun start(serverPort: Int, service: Service) {
        embeddedServer(factory = Netty, port = serverPort, module = generateModule(service)).start(wait = true)
    }
}

fun generateModule(service: Service): Application.() -> Unit = {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
    routing {
        route("/api") {
            route("/books") {
                post {
                    val createBookRequest = call.receive<CreateBookRequest>()
                    try {
                        val createdBookId = service.createBook(createBookRequest.title, createBookRequest.isbn)
                        call.respond(CreateBookResponse(createdBookId, createBookRequest.title, createBookRequest.isbn))
                    } catch (e: Exception) {
                        println("e: ${e.message}")
                        call.response.status(HttpStatusCode.InternalServerError)
                    }

                }
            }
        }
    }
}