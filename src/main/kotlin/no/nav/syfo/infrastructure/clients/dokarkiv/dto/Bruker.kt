package no.nav.syfo.infrastructure.clients.dokarkiv.dto

enum class BrukerIdType(
    val value: String,
) {
    PERSON_IDENT("FNR"),
    VIRKSOMHETSNUMMER("ORGNR"),
}

data class Bruker private constructor(
    val id: String,
    val idType: String,
) {
    companion object {
        fun create(
            id: String,
            idType: BrukerIdType,
        ) = Bruker(
            id = id,
            idType = idType.value
        )
    }
}
