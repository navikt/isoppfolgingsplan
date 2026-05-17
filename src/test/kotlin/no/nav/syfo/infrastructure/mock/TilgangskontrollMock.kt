package no.nav.syfo.infrastructure.mock

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import no.nav.syfo.UserConstants.ARBEIDSTAKER_PERSONIDENT_VEILEDER_NO_ACCESS
import no.nav.syfo.infrastructure.NAV_PERSONIDENT_HEADER

private const val TILGANGSKONTROLL_PERSON_PATH = "/api/tilgang/navident/person"
private const val TILGANGSKONTROLL_BRUKERE_PATH = "/api/tilgang/navident/brukere"

private data class TilgangResponse(
    val erGodkjent: Boolean,
    val erAvslatt: Boolean = false,
    val fullTilgang: Boolean = false,
)

fun MockRequestHandleScope.tilgangskontrollResponse(request: HttpRequestData): HttpResponseData {
    val requestUrl = request.url.encodedPath

    return when {
        requestUrl.endsWith(TILGANGSKONTROLL_PERSON_PATH) -> {
            when (request.headers[NAV_PERSONIDENT_HEADER]) {
                ARBEIDSTAKER_PERSONIDENT_VEILEDER_NO_ACCESS.value -> respond(TilgangResponse(erGodkjent = false))
                else -> respond(TilgangResponse(erGodkjent = true, fullTilgang = true))
            }
        }
        requestUrl.endsWith(TILGANGSKONTROLL_BRUKERE_PATH) -> {
            val body = runBlocking<List<String>> { request.receiveBody() }.toMutableList()
            body.removeAll { it == ARBEIDSTAKER_PERSONIDENT_VEILEDER_NO_ACCESS.value }
            respond(body)
        }
        else -> error("Unhandled path $requestUrl")
    }
}
