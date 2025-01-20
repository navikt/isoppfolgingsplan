package no.nav.syfo.infrastructure.clients.pdfgen

import no.nav.syfo.domain.DocumentComponent
import no.nav.syfo.domain.sanitizeForPdfGen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

sealed class PdfModel private constructor(
    val datoSendt: String,
    val documentComponents: List<DocumentComponent>,
) {
    private constructor(
        documentComponents: List<DocumentComponent>
    ) : this(
        datoSendt = LocalDate.now().format(formatter),
        documentComponents = documentComponents.sanitizeForPdfGen()
    )

    class ForesporselPdfModel(
        documentComponents: List<DocumentComponent>,
    ) : PdfModel(documentComponents = documentComponents)

    companion object {
        private val formatter = DateTimeFormatter.ofPattern("dd. MMMM yyyy", Locale("no", "NO"))
    }
}
