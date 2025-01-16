package no.nav.syfo.generator

import no.nav.syfo.UserConstants
import no.nav.syfo.domain.Foresporsel
import no.nav.syfo.domain.Personident
import no.nav.syfo.domain.Virksomhetsnummer

fun generateForsporsel(
    arbeidstakerPersonident: Personident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
    virksomhetsnummer: Virksomhetsnummer = UserConstants.VIRKSOMHETSNUMMER,
): Foresporsel =
    Foresporsel(
        arbeidstakerPersonident = arbeidstakerPersonident,
        veilederident = UserConstants.VEILEDER_IDENT,
        virksomhetsnummer = virksomhetsnummer,
        narmestelederPersonident = UserConstants.NARMESTELEDER_FNR,
    )
