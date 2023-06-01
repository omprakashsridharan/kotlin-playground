import org.ktorm.database.Database

class Resources() : AutoCloseable {

    var database: Database

    init {
        getDatabaseConnection(dbUrl, dbUser, dbPassword).let {
            database = it
        }
        println("Initialised all resources")
    }

    override fun close() {
        println("Closed all resources")
    }

}