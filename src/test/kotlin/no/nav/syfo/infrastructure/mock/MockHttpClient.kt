package no.nav.syfo.infrastructure.mock

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import no.nav.syfo.Environment
import no.nav.syfo.common.http.commonConfig
import no.nav.syfo.common.mock.tilgangskontroll.mockTilgangskontrollRequestHandler
import no.nav.syfo.common.mock.token.azuread.mockAzureAdRequestHandler

fun mockHttpClient(environment: Environment) =
    HttpClient(MockEngine) {
        commonConfig()
        engine {
            addHandler { request ->
                val requestUrl = request.url.encodedPath
                when {
                    requestUrl == "/${environment.azure.openidConfigTokenEndpoint}" ->
                        mockAzureAdRequestHandler(request)
                    requestUrl.startsWith("/${environment.clients.istilgangskontroll.baseUrl}") ->
                        mockTilgangskontrollRequestHandler(request, mockTilgangDetailsPerNavIdent)
                    requestUrl.startsWith("/${environment.clients.ereg.baseUrl}") ->
                        eregMockResponse(request)
                    requestUrl.startsWith("/${environment.clients.dokarkiv.baseUrl}") ->
                        dokarkivMockResponse(request)
                    requestUrl.startsWith("/${environment.clients.ispdfgen.baseUrl}") ->
                        pdfGenMockResponse(request)
                    else -> error("Unhandled ${request.url.encodedPath}")
                }
            }
        }
    }
