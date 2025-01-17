package no.nav.syfo.infrastructure.clients.pdfgen

import kotlinx.coroutines.runBlocking
import no.nav.syfo.ExternalMockEnvironment
import no.nav.syfo.UserConstants
import no.nav.syfo.generator.generateDocumentComponent
import no.nav.syfo.shouldBeEqualTo
import org.junit.jupiter.api.Test

class PdfGenClientSpek {
    val externalMockEnvironment = ExternalMockEnvironment.instance
    val pdfGenClient = externalMockEnvironment.pdfClient

    @Test
    fun `returns bytearray of pdf for foresporsel`() {
        val pdf = runBlocking {
            pdfGenClient.createForesporselPdf(
                payload = PdfModel.ForesporselPdfModel(
                    documentComponents = generateDocumentComponent(),
                )
            )
        }

        pdf.size shouldBeEqualTo UserConstants.PDF_FORESPORSEL.size
        pdf.forEachIndexed { index, b -> b shouldBeEqualTo UserConstants.PDF_FORESPORSEL[index] }
    }
}
