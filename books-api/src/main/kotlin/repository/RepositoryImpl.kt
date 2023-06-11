package repository

import database.getDatabaseConnection
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync

class RepositoryImpl(
    jdbcUrl: String,
    driverClassName: String,
    username: String,
    password: String,
    private val tracer: Tracer
) : Repository {

    private var database: Database

    init {
        database = getDatabaseConnection(
            jdbcUrl = jdbcUrl,
            driverClassName = driverClassName,
            username = username,
            password = password
        )
    }

    override suspend fun createBook(title: String, isbn: String): Result<Long> {
        val createBookRepositorySpan =
            tracer.spanBuilder("createBookRepository").setSpanKind(SpanKind.INTERNAL)
                .setParent(Context.current())
                .startSpan()
        try {
            val createdBookId = suspendedTransactionAsync(Dispatchers.IO, db = database) {
                Books.insert {
                    it[Books.title] = title
                    it[Books.isbn] = isbn
                } get Books.id
            }.await()
            createBookRepositorySpan.setStatus(StatusCode.OK)
            return Result.success(createdBookId)
        } catch (e: Exception) {
            e.message?.let { createBookRepositorySpan.setAttribute("repository.create.book.error", it) }
            createBookRepositorySpan.setStatus(StatusCode.ERROR)
            return Result.failure(e)
        } finally {
            createBookRepositorySpan.end()
        }
    }
}