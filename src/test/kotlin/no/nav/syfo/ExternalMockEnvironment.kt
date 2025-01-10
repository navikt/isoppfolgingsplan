package no.nav.syfo

import io.mockk.mockk
import no.nav.syfo.application.IVarselProducer
import no.nav.syfo.infrastructure.clients.azuread.AzureAdClient
import no.nav.syfo.infrastructure.clients.dokarkiv.DokarkivClient
import no.nav.syfo.infrastructure.clients.ereg.EregClient
import no.nav.syfo.infrastructure.clients.wellknown.WellKnown
import no.nav.syfo.infrastructure.database.TestDatabase
import no.nav.syfo.infrastructure.database.repository.ForesporselRepository
import no.nav.syfo.infrastructure.mock.mockHttpClient
import java.nio.file.Paths

fun wellKnownInternalAzureAD(): WellKnown {
    val path = "src/test/resources/jwkset.json"
    val uri = Paths.get(path).toUri().toURL()
    return WellKnown(
        issuer = "https://sts.issuer.net/veileder/v2",
        jwksUri = uri.toString(),
    )
}

class ExternalMockEnvironment private constructor() {
    val applicationState: ApplicationState = testAppState()
    val database = TestDatabase()
    val environment = testEnvironment()
    val mockHttpClient = mockHttpClient(environment = environment)
    val wellKnownInternalAzureAD = wellKnownInternalAzureAD()
    val azureAdClient =
        AzureAdClient(
            azureEnvironment = environment.azure,
            httpClient = mockHttpClient,
        )
    val eregClient =
        EregClient(
            baseUrl = environment.clients.ereg.baseUrl,
            httpClient = mockHttpClient,
        )
    val dokarkivClient =
        DokarkivClient(
            azureAdClient = azureAdClient,
            dokarkivEnvironment = environment.clients.dokarkiv,
            httpClient = mockHttpClient,
        )
    val varselProducer = mockk<IVarselProducer>(relaxed = true)
    val foresporselRepository = ForesporselRepository(database)

    companion object {
        val instance: ExternalMockEnvironment = ExternalMockEnvironment()
    }
}
