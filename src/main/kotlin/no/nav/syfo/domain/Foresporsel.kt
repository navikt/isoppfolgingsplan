package no.nav.syfo.domain

import no.nav.syfo.util.nowUTC
import java.time.OffsetDateTime
import java.util.UUID

@ConsistentCopyVisibility
data class Foresporsel private constructor(
    val uuid: UUID,
    val createdAt: OffsetDateTime,
    val arbeidstakerPersonident: Personident,
    val veilederident: Veilederident,
    val virksomhetsnummer: Virksomhetsnummer,
    val narmestelederPersonident: Personident,
) {
    constructor(
        arbeidstakerPersonident: Personident,
        veilederident: Veilederident,
        virksomhetsnummer: Virksomhetsnummer,
        narmestelederPersonident: Personident,
    ) : this(
        uuid = UUID.randomUUID(),
        createdAt = nowUTC(),
        arbeidstakerPersonident = arbeidstakerPersonident,
        veilederident = veilederident,
        virksomhetsnummer = virksomhetsnummer,
        narmestelederPersonident = narmestelederPersonident,
    )

    companion object {
        fun createFromDatabase(
            uuid: UUID,
            createdAt: OffsetDateTime,
            arbeidstakerPersonident: Personident,
            veilederident: Veilederident,
            virksomhetsnummer: Virksomhetsnummer,
            narmestelederPersonident: Personident,
        ) = Foresporsel(
            uuid = uuid,
            createdAt = createdAt,
            arbeidstakerPersonident = arbeidstakerPersonident,
            veilederident = veilederident,
            virksomhetsnummer = virksomhetsnummer,
            narmestelederPersonident = narmestelederPersonident,
        )
    }
}
