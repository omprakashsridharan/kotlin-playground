plugins {
    id("application-conventions")
}

dependencies {

    implementation(project(mapOf("path" to ":consumer")))
    implementation(project(mapOf("path" to ":common")))

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("MainKt")
}