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
import io.opentelemetry.api.trace.Span
import io.opentelemetry.extension.kotlin.asContextElement
import io.opentelemetry.instrumentation.ktor.v2_0.server.KtorServerTracing
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import request.dto.CreateBookRequest
import response.dto.CreateBookResponse
import service.Service

class Server(private val openTelemetry: OpenTelemetry, private val service: Service) {

    fun start(serverPort: Int) {
        embeddedServer(factory = Netty, port = serverPort, module = generateModule()).start(wait = true)
    }

    private fun generateModule(): Application.() -> Unit = {
        log.info("")
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
                        val currentSpanContext = Span.current().asContextElement()
                        try {
                            withContext(currentSpanContext) {
                                Span.current().setAttribute("create.book.title", createBookRequest.title)
                                Span.current().setAttribute("create.book.isbn", createBookRequest.isbn)

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
                                call.application.environment.log.info("create book response sent")
                            }
                        } catch (e: Exception) {
                            call.application.environment.log.error("e: ${e.message}")
                            call.response.status(HttpStatusCode.InternalServerError)
                        }
                    }
                }
            }
        }
    }
}

