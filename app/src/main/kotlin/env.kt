import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.cloudnative.env.Secret
import org.http4k.lens.Lens
import org.http4k.lens.int
import org.http4k.lens.secret
import org.http4k.lens.string

data class Port(val value: Int)
data class DbUrl(val value: String)
data class DbUser(val value: String)

val env = Environment.ENV

val portLens: Lens<Environment, Port> = EnvironmentKey.int().map(::Port).defaulted("PORT", Port(9000))
val dbUrlLens: Lens<Environment, DbUrl> = EnvironmentKey.string().map(::DbUrl).required("DB_URL")
val dbUserLens: Lens<Environment, DbUser> = EnvironmentKey.string().map(::DbUser).required("DB_USER")
val dbPasswordLens: Lens<Environment, Secret> = EnvironmentKey.secret().required("DB_PASSWORD")


val port: Port = portLens(env)
val dbUrl: DbUrl = dbUrlLens(env)
val dbUser: DbUser = dbUserLens(env)
val dbPassword: Secret = dbPasswordLens(env)