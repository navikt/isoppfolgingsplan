package no.nav.syfo.domain

import java.time.OffsetDateTime
import java.util.UUID

@ConsistentCopyVisibility
data class Foresporsel(
    val uuid: UUID,
    val createdAt: OffsetDateTime,
    val arbeidstakerPersonident: Personident,
    val veilederident: Veilederident,
    val virksomhetsnummer: Virksomhetsnummer,
    val narmestelederPersonident: Personident,
)
