package no.nav.syfo

import no.nav.syfo.domain.Personident
import no.nav.syfo.domain.Veilederident
import no.nav.syfo.domain.Virksomhetsnummer

object UserConstants {
    val ARBEIDSTAKER_PERSONIDENT = Personident("12345678910")
    val ARBEIDSTAKER_PERSONIDENT_2 = Personident("12345678911")
    val ARBEIDSTAKER_PERSONIDENT_VEILEDER_NO_ACCESS = Personident("11111111111")
    val VIRKSOMHETSNUMMER = Virksomhetsnummer("981111117")
    val NARMESTELEDER_FNR = Personident("98765432101")
    val VEILEDER_IDENT = Veilederident("Z999999")
}
