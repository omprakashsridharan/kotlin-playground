package server

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


// TODO: extract it to common/domain
interface Service : AutoCloseable {
    suspend fun createBook(title: String, isbn: String): Long
}

class Server {
    fun start(serverPort: Int, service: Service) {
        embeddedServer(factory = Netty, port = serverPort, module = generateModule(service)).start(wait = true)
    }
}


@Serializable
data class CreateBookRequest(val title: String, val isbn: String)

@Serializable
data class CreateBookResponse(val id: Long, val title: String, val isbn: String)

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