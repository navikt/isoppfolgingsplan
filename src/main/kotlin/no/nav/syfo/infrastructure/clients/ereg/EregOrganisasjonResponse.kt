package no.nav.syfo.infrastructure.clients.ereg

data class EregOrganisasjonNavn(
    val navnelinje1: String,
    val redigertnavn: String?,
)

data class EregOrganisasjonResponse(
    val navn: EregOrganisasjonNavn,
)

fun EregOrganisasjonResponse.toEregVirksomhetsnavn(): EregVirksomhetsnavn =
    EregVirksomhetsnavn(
        virksomhetsnavn =
            this.navn.let { (navnelinje1, redigertnavn) ->
                if (redigertnavn.isNullOrBlank()) navnelinje1 else redigertnavn
            }
    )
