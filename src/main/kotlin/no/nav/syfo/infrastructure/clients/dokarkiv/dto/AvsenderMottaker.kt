package no.nav.syfo.infrastructure.clients.dokarkiv.dto

data class AvsenderMottaker private constructor(
    val id: String?,
    val idType: String?,
    val navn: String? = null,
) {
    companion object {
        fun create(
            id: String?,
            idType: BrukerIdType?,
            navn: String? = null,
        ) = AvsenderMottaker(
            id = id,
            idType = idType?.value,
            navn = navn
        )
    }
}
