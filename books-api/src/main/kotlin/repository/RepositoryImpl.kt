package repository

import database.getDatabaseConnection
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction
import tracing.trace

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
        transaction {
            SchemaUtils.create(Books)
        }

    }

    override suspend fun createBook(title: String, isbn: String): Result<Long> {


        return tracer.trace("createBookRepository") {
            try {
                val createdBookId = suspendedTransactionAsync(Dispatchers.IO, db = database) {
                    Books.insert {
                        it[Books.title] = title
                        it[Books.isbn] = isbn
                    } get Books.id
                }.await()
                it.setStatus(StatusCode.OK)
                return@trace Result.success(createdBookId)
            } catch (e: Exception) {
                e.message?.let { m -> it.setAttribute("repository.create.book.error", m) }
                it.setStatus(StatusCode.ERROR)
                return@trace Result.failure(e)
            } finally {
                it.end()
            }
        }


    }
}