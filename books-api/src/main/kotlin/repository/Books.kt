package repository

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Books: Table() {
    val id: Column<Long> = long("id").autoIncrement()
    val title: Column<String> = varchar("title", length = 50)
    val isbn: Column<String> = varchar("isbn", length = 50)

    override val primaryKey = PrimaryKey(id, name = "PK_Books_ID")
}