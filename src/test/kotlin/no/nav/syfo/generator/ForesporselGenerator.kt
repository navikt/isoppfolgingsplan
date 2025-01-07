package no.nav.syfo.generator

import no.nav.syfo.UserConstants
import no.nav.syfo.domain.Foresporsel

fun generateForsporsel(): Foresporsel =
    Foresporsel(
        arbeidstakerPersonident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
        veilederident = UserConstants.VEILEDER_IDENT,
        virksomhetsnummer = UserConstants.VIRKSOMHETSNUMMER,
        narmestelederPersonident = UserConstants.NARMESTELEDER_FNR,
    )
