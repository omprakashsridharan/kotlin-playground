

plugins {
    id("library-conventions")
}

val kafkaApiVersion: String by project
val testcontainersVersion: String by project
dependencies {
    implementation("org.apache.kafka:kafka-clients:$kafkaApiVersion")
    implementation("io.confluent:kafka-json-schema-serializer:7.4.0")
    implementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:kafka:$testcontainersVersion")

}

tasks.test {
    useJUnitPlatform()
}
