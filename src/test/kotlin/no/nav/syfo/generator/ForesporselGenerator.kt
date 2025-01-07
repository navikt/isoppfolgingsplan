package no.nav.syfo.generator

import no.nav.syfo.UserConstants
import no.nav.syfo.domain.Foresporsel
import no.nav.syfo.util.nowUTC
import java.util.*

fun generateForsporsel(): Foresporsel =
    Foresporsel(
        uuid = UUID.randomUUID(),
        createdAt = nowUTC(),
        arbeidstakerPersonident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
        veilederident = UserConstants.VEILEDER_IDENT,
        virksomhetsnummer = UserConstants.VIRKSOMHETSNUMMER,
        narmestelederPersonident = UserConstants.NARMESTELEDER_FNR,
    )
