package no.nav.syfo.generator

import no.nav.syfo.UserConstants
import no.nav.syfo.domain.Foresporsel
import no.nav.syfo.domain.Personident

fun generateForsporsel(arbeidstakerPersonident: Personident = UserConstants.ARBEIDSTAKER_PERSONIDENT): Foresporsel =
    Foresporsel(
        arbeidstakerPersonident = arbeidstakerPersonident,
        veilederident = UserConstants.VEILEDER_IDENT,
        virksomhetsnummer = UserConstants.VIRKSOMHETSNUMMER,
        narmestelederPersonident = UserConstants.NARMESTELEDER_FNR,
    )
