import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.instrumentation.ktor.v2_0.server.KtorServerTracing
import kotlinx.serialization.json.Json
import request.dto.CreateBookRequest
import response.dto.CreateBookResponse
import service.Service
import tracing.trace

class Server(private val openTelemetry: OpenTelemetry, private val service: Service, private val tracer: Tracer) {


    fun start(serverPort: Int) {
        embeddedServer(factory = Netty, port = serverPort, module = generateModule()).start(wait = true)
    }

    private fun generateModule(): Application.() -> Unit = {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
        install(CallLogging)
        install(CallId) {
            header(HttpHeaders.XRequestId)
            verify { callId: String ->
                callId.isNotEmpty()
            }
        }
        install(KtorServerTracing) {
            setOpenTelemetry(openTelemetry)
        }
        routing {
            route("/api") {
                route("/books") {
                    post {
                        call.application.environment.log.info("create book request received")
                        val createBookRequest = call.receive<CreateBookRequest>()

                        tracer.trace("createBookApi") {
                            try {
                                it.setAttribute("create.book.title", createBookRequest.title)
                                it.setAttribute("create.book.isbn", createBookRequest.isbn)
                                val createdBookResult =
                                    service.createBook(createBookRequest.title, createBookRequest.isbn)
                                val createdBookId = createdBookResult.getOrThrow()
                                call.respond(
                                    CreateBookResponse(
                                        createdBookId,
                                        createBookRequest.title,
                                        createBookRequest.isbn
                                    )
                                )
                                it.setStatus(StatusCode.OK)
                            } catch (e: Exception) {
                                it.setStatus(StatusCode.ERROR)
                                call.application.environment.log.error("e: ${e.message}")
                                call.response.status(HttpStatusCode.InternalServerError)
                            }
                        }

                    }
                }
            }
        }
    }
}

