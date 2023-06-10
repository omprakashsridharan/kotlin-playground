package repository

import database.getDatabaseConnection
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync

class RepositoryImpl(jdbcUrl: String, driverClassName: String, username: String, password: String) : Repository {

    private var database: Database
    init {
        database = getDatabaseConnection(
            jdbcUrl = jdbcUrl,
            driverClassName = driverClassName,
            username = username,
            password = password
        )
    }

    override suspend fun createBook(title: String, isbn: String) =
        suspendedTransactionAsync(Dispatchers.IO, db = database) {
            Books.insert {
                it[Books.title] = title
                it[Books.isbn] = isbn
            } get Books.id
        }

}