import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.Lens
import org.http4k.lens.int

data class Port(val value: Int)

val env = Environment.ENV

val portLens: Lens<Environment, Port> = EnvironmentKey.int().map(::Port).defaulted("PORT", Port(9000))
val port: Port = portLens(env)