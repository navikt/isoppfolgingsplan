package no.nav.syfo.api

import io.ktor.server.application.*
import no.nav.syfo.ExternalMockEnvironment
import no.nav.syfo.application.ForesporselService
import no.nav.syfo.infrastructure.clients.dokarkiv.DokarkivClient
import no.nav.syfo.infrastructure.clients.ereg.EregClient
import no.nav.syfo.infrastructure.clients.pdfgen.PdfGenClient
import no.nav.syfo.infrastructure.clients.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.infrastructure.database.repository.ForesporselRepository
import no.nav.syfo.infrastructure.journalforing.JournalforingService

fun Application.testApiModule(externalMockEnvironment: ExternalMockEnvironment) {
    val database = externalMockEnvironment.database
    val varselProducer = externalMockEnvironment.varselProducer

    val veilederTilgangskontrollClient =
        VeilederTilgangskontrollClient(
            azureAdClient = externalMockEnvironment.azureAdClient,
            clientEnvironment = externalMockEnvironment.environment.clients.istilgangskontroll,
            httpClient = externalMockEnvironment.mockHttpClient,
        )
    val dokarkivClient =
        DokarkivClient(
            azureAdClient = externalMockEnvironment.azureAdClient,
            dokarkivEnvironment = externalMockEnvironment.environment.clients.dokarkiv,
            httpClient = externalMockEnvironment.mockHttpClient,
        )
    val eregClient =
        EregClient(
            baseUrl = externalMockEnvironment.environment.clients.ereg.baseUrl,
            httpClient = externalMockEnvironment.mockHttpClient,
        )
    val pdfClient =
        PdfGenClient(
            pdfGenBaseUrl = externalMockEnvironment.environment.clients.ispdfgen.baseUrl,
            httpClient = externalMockEnvironment.mockHttpClient,
        )
    val journalforingService =
        JournalforingService(
            dokarkivClient = dokarkivClient,
            eregClient = eregClient,
            pdfClient = pdfClient,
            isJournalforingRetryEnabled = externalMockEnvironment.environment.isJournalforingRetryEnabled,
        )
    val foresporselService =
        ForesporselService(
            varselProducer = varselProducer,
            repository = ForesporselRepository(database),
            journalforingService = journalforingService,
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
