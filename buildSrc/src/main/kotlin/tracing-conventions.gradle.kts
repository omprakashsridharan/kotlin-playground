plugins {
    id("common-conventions")
    `java-library`
}

val openTelemetryVersion: String = "1.27.0"
dependencies {
    implementation("io.opentelemetry:opentelemetry-api:$openTelemetryVersion")
    implementation("io.opentelemetry:opentelemetry-sdk:$openTelemetryVersion")
    implementation("io.opentelemetry:opentelemetry-exporter-zipkin:$openTelemetryVersion")
    implementation("io.opentelemetry:opentelemetry-semconv:1.27.0-alpha")
}