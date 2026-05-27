package no.nav.syfo.infrastructure.clients

import no.nav.syfo.common.util.ClientConfig
import no.nav.syfo.common.util.OpenClientConfig

data class ClientsConfig(
    val istilgangskontroll: ClientConfig,
    val dokarkiv: ClientConfig,
    val ereg: OpenClientConfig,
    val ispdfgen: OpenClientConfig,
)
