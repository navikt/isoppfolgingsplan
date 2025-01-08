package no.nav.syfo.api.model

import no.nav.syfo.domain.Foresporsel
import java.time.OffsetDateTime
import java.util.*

data class ForesporselResponseDTO(
    val uuid: UUID,
    val createdAt: OffsetDateTime,
    val arbeidstakerPersonident: String,
    val veilederident: String,
    val virksomhetsnummer: String,
    val narmestelederPersonident: String,
) {
    companion object {
        fun fromForesporsel(foresporsel: Foresporsel) =
            ForesporselResponseDTO(
                uuid = foresporsel.uuid,
                createdAt = foresporsel.createdAt,
                arbeidstakerPersonident = foresporsel.arbeidstakerPersonident.value,
                veilederident = foresporsel.veilederident.value,
                virksomhetsnummer = foresporsel.virksomhetsnummer.value,
                narmestelederPersonident = foresporsel.narmestelederPersonident.value,
            )
    }
}
