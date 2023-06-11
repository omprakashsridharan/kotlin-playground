plugins {
    id("org.jetbrains.kotlin.jvm")
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

dependencies {
    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.5.1")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")
    implementation("ch.qos.logback:logback-classic:1.4.7")


}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.test {
    useJUnitPlatform()
}