plugins {
    id("application-conventions")
    id("tracing-conventions")
}

dependencies {

    implementation(project(mapOf("path" to ":consumer")))
    implementation(project(mapOf("path" to ":common")))
    implementation(project(":tracing"))
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("MainKt")
}