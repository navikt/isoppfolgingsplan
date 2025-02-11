package no.nav.syfo

import no.nav.syfo.domain.Personident
import no.nav.syfo.domain.Veilederident
import no.nav.syfo.domain.Virksomhetsnummer
import java.util.*

object UserConstants {
    val ARBEIDSTAKER_PERSONIDENT = Personident("12345678910")
    val ARBEIDSTAKER_PERSONIDENT_2 = Personident("12345678911")
    val ARBEIDSTAKER_PERSONIDENT_VEILEDER_NO_ACCESS = Personident("11111111111")
    val VIRKSOMHETSNUMMER = Virksomhetsnummer("981111117")
    val OTHER_VIRKSOMHETSNUMMER = Virksomhetsnummer("981111127")
    val VIRKSOMHETSNAVN = "Bedriften"
    val NARMESTELEDER_FNR = Personident("98765432101")
    val OTHER_NARMESTELEDER_FNR = Personident("98765432102")
    val VEILEDER_IDENT = Veilederident("Z999999")

    const val VIRKSOMHETSNUMMER_2 = "123456781"

    val VIRKSOMHETSNUMMER_NO_VIRKSOMHETSNAVN = Virksomhetsnummer(VIRKSOMHETSNUMMER.value.replace("1", "3"))

    val EXISTING_EKSTERN_REFERANSE_UUID: UUID = UUID.fromString("e7e8e9e0-e1e2-e3e4-e5e6-e7e8e9e0e1e2")
    val FAILING_EKSTERN_REFERANSE_UUID: UUID = UUID.randomUUID()

    val PDF_FORESPORSEL = byteArrayOf(0x2E, 0x28)
}
