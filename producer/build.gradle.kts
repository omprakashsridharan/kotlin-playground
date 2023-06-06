

plugins {
    id("library-conventions")
    kotlin("kapt")
    kotlin("plugin.serialization") version "1.8.21"
}

val kafkaApiVersion: String by project
val testcontainersVersion: String by project
dependencies {
    implementation("org.apache.kafka:kafka-clients:$kafkaApiVersion")

    implementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:kafka:$testcontainersVersion")

}

tasks.test {
    useJUnitPlatform()
}
