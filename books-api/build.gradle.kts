
plugins {
    id("application-conventions")
    id("api-conventions")
    id("database-conventions")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.0"
}

dependencies {

    implementation(project(":database"))
    implementation(project(mapOf("path" to ":producer")))
    implementation(project(mapOf("path" to ":common")))

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("MainKt")
}