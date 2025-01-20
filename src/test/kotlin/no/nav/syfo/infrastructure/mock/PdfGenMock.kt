package no.nav.syfo.infrastructure.mock

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import no.nav.syfo.UserConstants
import no.nav.syfo.infrastructure.clients.pdfgen.PdfGenClient

fun MockRequestHandleScope.pdfGenMockResponse(request: HttpRequestData): HttpResponseData {
    val requestUrl = request.url.encodedPath

    return when {
        requestUrl.endsWith(PdfGenClient.Companion.FORESPORSEL_PATH) -> {
            respond(content = UserConstants.PDF_FORESPORSEL)
        }

        else -> error("Unhandled pdf $requestUrl")
    }
}
