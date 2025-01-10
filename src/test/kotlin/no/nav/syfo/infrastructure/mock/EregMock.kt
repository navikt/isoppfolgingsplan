package no.nav.syfo.infrastructure.mock

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.syfo.UserConstants
import no.nav.syfo.UserConstants.VIRKSOMHETSNUMMER
import no.nav.syfo.UserConstants.VIRKSOMHETSNUMMER_2
import no.nav.syfo.UserConstants.VIRKSOMHETSNUMMER_NO_VIRKSOMHETSNAVN
import no.nav.syfo.infrastructure.clients.ereg.EregOrganisasjonNavn
import no.nav.syfo.infrastructure.clients.ereg.EregOrganisasjonResponse

val eregOrganisasjonResponse =
    EregOrganisasjonResponse(
        navn =
            EregOrganisasjonNavn(
                navnelinje1 = UserConstants.VIRKSOMHETSNAVN,
                redigertnavn = UserConstants.VIRKSOMHETSNAVN,
            )
    )

fun MockRequestHandleScope.eregMockResponse(request: HttpRequestData): HttpResponseData {
    val requestUrl = request.url.encodedPath

    return when {
        requestUrl.endsWith(VIRKSOMHETSNUMMER.value) -> respond(eregOrganisasjonResponse)
        requestUrl.endsWith(VIRKSOMHETSNUMMER_2) -> respond(eregOrganisasjonResponse)
        requestUrl.endsWith(VIRKSOMHETSNUMMER_NO_VIRKSOMHETSNAVN.value) -> respondError(status = HttpStatusCode.InternalServerError)
        else -> error("Unhandled path $requestUrl")
    }
}
