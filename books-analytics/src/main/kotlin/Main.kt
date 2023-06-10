import common.utils.awaitShutdown
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    val bookCreatedConsumer = BookCreatedConsumer(kafkaBootstrapServers, schemaRegistryUrl)
    val service: Service = ServiceImpl()
    val bookCreatedConsumerJob = launch {
        bookCreatedConsumer.consumeCreatedBook { createdBook ->
            println("Consumed book $createdBook")
            service.incrementBookCount()
        }.cancellable().collect()
    }
    awaitShutdown()
    bookCreatedConsumerJob.cancelAndJoin()
}
