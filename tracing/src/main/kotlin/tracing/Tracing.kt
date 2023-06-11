package tracing

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;


fun initializeOpenTelemetry(endpoint: String, serviceName: String): OpenTelemetry {
    val zipkinExporter = ZipkinSpanExporter.builder().setEndpoint(endpoint).build();
    val serviceNameResource: Resource = Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, serviceName))
    val tracerProvider = SdkTracerProvider.builder()
        .addSpanProcessor(SimpleSpanProcessor.create(zipkinExporter))
        .setResource(Resource.getDefault().merge(serviceNameResource))
        .build()
    return OpenTelemetrySdk.builder().setTracerProvider(tracerProvider)
        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
        .buildAndRegisterGlobal()
}