import common.utils.awaitShutdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    val bookCreatedConsumer = BookCreatedConsumer(kafkaBootstrapServers, schemaRegistryUrl)
    val service: Service = ServiceImpl()
    val bookCreatedConsumerJob = launch(Dispatchers.IO) {
        bookCreatedConsumer.consumeCreatedBook().cancellable().collect() { createdBook ->
            println("Consumed book $createdBook")
            service.incrementBookCount()
        }
    }
    awaitShutdown()
    bookCreatedConsumerJob.cancelAndJoin()
}
