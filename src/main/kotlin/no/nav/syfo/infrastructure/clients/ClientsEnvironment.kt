package no.nav.syfo.infrastructure.clients

data class ClientsEnvironment(
    val istilgangskontroll: ClientEnvironment
)

data class ClientEnvironment(
    val baseUrl: String,
    val clientId: String
)

data class OpenClientEnvironment(
    val baseUrl: String
)
