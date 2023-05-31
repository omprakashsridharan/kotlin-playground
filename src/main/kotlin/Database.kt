import org.http4k.cloudnative.env.Secret
import org.ktorm.database.Database
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import org.ktorm.support.postgresql.PostgreSqlDialect

object Tables {
    object Books : Table<Nothing>("t_book") {
        val id = long("id").primaryKey()
        val title = varchar("tile")
        val isbn = varchar("isbn")
    }
}

fun getDatabaseConnection(dbUrl: DbUrl, dbUser: DbUser, dbPassword: Secret): Database? = dbPassword.use { password ->
    try {
        return@use Database.connect(
            dbUrl.value,
            user = dbUser.value,
            password = password,
            dialect = PostgreSqlDialect()
        )
    } catch (e: Exception) {
        println("Error while connecting to database: ${e.message}")
        return@use null
    }
}