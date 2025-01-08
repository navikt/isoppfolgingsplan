package no.nav.syfo.api

import io.ktor.server.application.*
import no.nav.syfo.ExternalMockEnvironment
import no.nav.syfo.application.ForesporselService
import no.nav.syfo.infrastructure.clients.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.infrastructure.database.repository.ForesporselRepository

fun Application.testApiModule(externalMockEnvironment: ExternalMockEnvironment) {
    val database = externalMockEnvironment.database
    val varselProducer = externalMockEnvironment.varselProducer

    val veilederTilgangskontrollClient =
        VeilederTilgangskontrollClient(
            azureAdClient = externalMockEnvironment.azureAdClient,
            clientEnvironment = externalMockEnvironment.environment.clients.istilgangskontroll,
            httpClient = externalMockEnvironment.mockHttpClient,
        )
    val foresporselService =
        ForesporselService(
            varselProducer = varselProducer,
            repository = ForesporselRepository(database),
        )

    this.apiModule(
        applicationState = externalMockEnvironment.applicationState,
        environment = externalMockEnvironment.environment,
        wellKnownInternalAzureAD = externalMockEnvironment.wellKnownInternalAzureAD,
        database = database,
        veilederTilgangskontrollClient = veilederTilgangskontrollClient,
        foresporselService = foresporselService,
    )
}
