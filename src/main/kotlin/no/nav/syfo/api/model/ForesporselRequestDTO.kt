package no.nav.syfo.api.model

import no.nav.syfo.domain.DocumentComponent

data class ForesporselRequestDTO(
    val arbeidstakerPersonident: String,
    val virksomhetsnummer: String,
    val narmestelederPersonident: String,
    val document: List<DocumentComponent>,
)
