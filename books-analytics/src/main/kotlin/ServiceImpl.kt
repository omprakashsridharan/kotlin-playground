class ServiceImpl() : Service {
    override suspend fun incrementBookCount(): Long {
        println("Book incremented")
        return 1
    }

}