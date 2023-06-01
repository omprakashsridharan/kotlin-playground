plugins {
    id("application-conventions")
}

dependencies {
    implementation(platform("org.http4k:http4k-bom:4.45.0.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-jetty")
    implementation("org.http4k:http4k-format-jackson")
    implementation("org.http4k:http4k-contract")
    implementation("org.http4k:http4k-cloudnative")

    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")

    //Database
    implementation("org.ktorm:ktorm-core:3.6.0")
    implementation("org.ktorm:ktorm-support-postgresql:3.6.0")
    implementation("org.postgresql:postgresql:42.6.0")
    testImplementation(kotlin("test"))

    // subprojects
    implementation(project(mapOf("path" to ":database")))
}

application {
    mainClass.set("MainKt")
}