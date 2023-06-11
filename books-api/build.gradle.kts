
plugins {
    id("application-conventions")
    id("api-conventions")
    id("database-conventions")
    id("tracing-conventions")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.0"
}

dependencies {

    implementation(project(":database"))
    implementation(project(":tracing"))
    implementation(project(mapOf("path" to ":producer")))
    implementation(project(mapOf("path" to ":common")))
    implementation("io.ktor:ktor-server-call-logging-jvm:2.3.1")
    implementation("io.ktor:ktor-server-call-id-jvm:2.3.1")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("MainKt")
}