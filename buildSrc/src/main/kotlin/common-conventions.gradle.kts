plugins {
    id("org.jetbrains.kotlin.jvm")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

dependencies {
    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.21")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")

}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.test{
    useJUnitPlatform()
}