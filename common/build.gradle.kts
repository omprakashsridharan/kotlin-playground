plugins {
    id("library-conventions")
    kotlin("kapt")
    kotlin("plugin.serialization") version "1.8.21"
}

dependencies {

}

tasks.test {
    useJUnitPlatform()
}
