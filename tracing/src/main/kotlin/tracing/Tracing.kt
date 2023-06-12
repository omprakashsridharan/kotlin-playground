package tracing

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter
import io.opentelemetry.extension.kotlin.asContextElement
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import kotlinx.coroutines.withContext


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

suspend fun <T> Tracer.trace(
    spanName: String,
    parentContext: Context = Context.current(),
    block: suspend (Span) -> T
): T {
    val span = spanBuilder(spanName).setSpanKind(SpanKind.INTERNAL).setParent(parentContext).startSpan()

    return try {
        withContext(span.asContextElement()) {
            block(span)
        }
    } finally {
        span.end()
    }
}

