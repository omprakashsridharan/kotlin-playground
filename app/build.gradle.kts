plugins {
    id("application-conventions")
}

dependencies {

    // database
    implementation(project(mapOf("path" to ":database")))

    // server
    implementation(project(mapOf("path" to ":server")))

    implementation(project(mapOf("path" to ":producer")))

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("MainKt")
}