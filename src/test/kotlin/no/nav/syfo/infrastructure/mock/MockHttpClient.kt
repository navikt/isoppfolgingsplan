package no.nav.syfo.infrastructure.mock

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import no.nav.syfo.Environment
import no.nav.syfo.infrastructure.clients.commonConfig

fun mockHttpClient(environment: Environment) =
    HttpClient(MockEngine) {
        commonConfig()
        engine {
            addHandler { request ->
                val requestUrl = request.url.encodedPath
                when {
                    requestUrl == "/${environment.azure.openidConfigTokenEndpoint}" -> azureAdMockResponse()
                    requestUrl.startsWith("/${environment.clients.istilgangskontroll.baseUrl}") ->
                        tilgangskontrollResponse(request)
                    requestUrl.startsWith("/${environment.clients.ereg.baseUrl}") ->
                        eregMockResponse(request)
                    requestUrl.startsWith("/${environment.clients.dokarkiv.baseUrl}") ->
                        dokarkivMockResponse(request)
                    else -> error("Unhandled ${request.url.encodedPath}")
                }
            }
        }
    }
