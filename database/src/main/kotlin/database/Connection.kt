package database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun getDatabaseConnection(jdbcUrl: String, driverClassName: String, username: String, password: String): Database {
    val config = HikariConfig().apply {
        this.jdbcUrl = jdbcUrl
        this.driverClassName = driverClassName
        this.username = username
        this.password = password
        this.maximumPoolSize = 10
    }
    val dataSource = HikariDataSource(config)

    val connection = Database.connect(dataSource)
    transaction {
        SchemaUtils.create(Books)
    }
    return connection
}