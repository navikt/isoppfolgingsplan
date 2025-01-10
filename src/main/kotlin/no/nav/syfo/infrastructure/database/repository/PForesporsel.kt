package no.nav.syfo.infrastructure.database.repository

import no.nav.syfo.domain.*
import java.time.OffsetDateTime
import java.util.*

data class PForesporsel(
    val id: Int,
    val uuid: UUID,
    val createdAt: OffsetDateTime,
    val arbeidstakerPersonident: Personident,
    val veilederident: Veilederident,
    val narmestelederPersonident: Personident,
    val virksomhetsnummer: Virksomhetsnummer,
    val publishedAt: OffsetDateTime?,
    val journalpostId: JournalpostId?,
) {
    fun toForesporsel() =
        Foresporsel.createFromDatabase(
            uuid = uuid,
            createdAt = createdAt,
            arbeidstakerPersonident = arbeidstakerPersonident,
            veilederident = veilederident,
            narmestelederPersonident = narmestelederPersonident,
            virksomhetsnummer = virksomhetsnummer,
            journalpostId = journalpostId,
        )
}
