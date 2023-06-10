plugins {
    id("application-conventions")
}

dependencies {

    // database
    implementation(project(mapOf("path" to ":database")))

    // server
    implementation(project(mapOf("path" to ":server")))

    implementation(project(mapOf("path" to ":producer")))
    implementation(project(mapOf("path" to ":common")))

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("MainKt")
}