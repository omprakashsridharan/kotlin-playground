

plugins {
    id("library-conventions")
    kotlin("kapt")
    kotlin("plugin.serialization") version "1.8.21"
}

val kafkaApiVersion: String by project
val testcontainersVersion: String by project
dependencies {
    implementation("org.apache.kafka:kafka-clients:$kafkaApiVersion")
    implementation("org.apache.avro:avro:1.11.1")
    implementation("io.confluent:kafka-avro-serializer:7.4.0")
    implementation("io.confluent:kafka-schema-registry:7.4.0")
    implementation("com.github.avro-kotlin.avro4k:avro4k-core:1.8.0")
    implementation("com.github.thake.avro4k:avro4k-kafka-serializer:0.14.0")
    implementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:kafka:$testcontainersVersion")

}

tasks.test {
    useJUnitPlatform()
}
