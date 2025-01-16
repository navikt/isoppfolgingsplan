package no.nav.syfo.api.model

data class ForesporselRequestDTO(
    val arbeidstakerPersonident: String,
    val virksomhetsnummer: String,
    val narmestelederPersonident: String,
)
