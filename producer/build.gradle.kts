plugins {
    id("library-conventions")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

val kafkaApiVersion: String by project
dependencies {
    implementation("org.apache.kafka:kafka-clients:$kafkaApiVersion")
    implementation("io.confluent:kafka-json-schema-serializer:7.4.0")
}