package producer

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.Network

import org.testcontainers.containers.wait.strategy.Wait


class SchemaRegistryContainer(version: String) :
    GenericContainer<SchemaRegistryContainer?>("$SCHEMA_REGISTRY_IMAGE:$version") {
    init {
        waitingFor(Wait.forHttp("/subjects").forStatusCode(200))
        withExposedPorts(SCHEMA_REGISTRY_PORT)
    }

    fun withKafka(kafka: KafkaContainer): SchemaRegistryContainer {
        return withKafka(kafka.network, kafka.networkAliases[0] + ":9092")
    }

    fun withKafka(network: Network?, bootstrapServers: String): SchemaRegistryContainer {
        withNetwork(network)
        withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
        withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
        withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://$bootstrapServers")
        return this
    }

    companion object {
        const val SCHEMA_REGISTRY_IMAGE = "confluentinc/cp-schema-registry"
        const val SCHEMA_REGISTRY_PORT = 8081
    }
}