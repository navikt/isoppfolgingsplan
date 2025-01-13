package no.nav.syfo.infrastructure.journalforing

import no.nav.syfo.application.IJournalforingService
import no.nav.syfo.domain.*
import no.nav.syfo.infrastructure.clients.ereg.EregClient
import no.nav.syfo.infrastructure.clients.dokarkiv.DokarkivClient
import no.nav.syfo.infrastructure.clients.dokarkiv.dto.*
import org.slf4j.LoggerFactory

class JournalforingService(
    private val dokarkivClient: DokarkivClient,
    private val eregClient: EregClient,
    private val isJournalforingRetryEnabled: Boolean,
) : IJournalforingService {
    override suspend fun journalfor(
        foresporsel: Foresporsel,
        pdf: ByteArray
    ): Result<JournalpostId> =
        runCatching {
            val journalpostRequest =
                createJournalpostRequest(
                    foresporsel = foresporsel,
                    pdf = pdf
                )

            val journalpostId =
                try {
                    dokarkivClient.journalfor(journalpostRequest).journalpostId
                } catch (exc: Exception) {
                    if (isJournalforingRetryEnabled) {
                        throw exc
                    } else {
                        log.error("Journalføring failed, skipping retry (should only happen in dev-gcp)", exc)
                        // Defaulting'en til DEFAULT_FAILED_JP_ID skal bare forekomme i dev-gcp:
                        // Har dette fordi vi ellers spammer ned dokarkiv med forsøk på å journalføre
                        // på personer som mangler aktør-id.
                        DEFAULT_FAILED_JP_ID
                    }
                }
            JournalpostId(journalpostId.toString())
        }

    private suspend fun createJournalpostRequest(
        foresporsel: Foresporsel,
        pdf: ByteArray,
    ): JournalpostRequest {
        val avsenderMottaker =
            AvsenderMottaker.create(
                id = foresporsel.virksomhetsnummer.value,
                idType = BrukerIdType.VIRKSOMHETSNUMMER,
                navn = eregClient.organisasjonVirksomhetsnavn(foresporsel.virksomhetsnummer)?.virksomhetsnavn ?: "",
            )
        val bruker =
            Bruker.create(
                id = foresporsel.arbeidstakerPersonident.value,
                idType = BrukerIdType.PERSON_IDENT,
            )

        val tittel = "Forespørsel til arbeidsgiver om oppfølgingsplan"
        val dokumenter =
            listOf(
                Dokument.create(
                    brevkode = BrevkodeType.FORESPORSEL_OPPFOLGINGSPLAN,
                    dokumentvarianter =
                        listOf(
                            Dokumentvariant.create(
                                filnavn = tittel,
                                filtype = FiltypeType.PDFA,
                                fysiskDokument = pdf,
                                variantformat = VariantformatType.ARKIV,
                            )
                        ),
                    tittel = tittel,
                ),
            )

        return JournalpostRequest(
            avsenderMottaker = avsenderMottaker,
            tittel = "Forespørsel til arbeidsgiver om oppfølgingsplan",
            bruker = bruker,
            dokumenter = dokumenter,
            overstyrInnsynsregler = OverstyrInnsynsregler.VISES_MASKINELT_GODKJENT.name,
            eksternReferanseId = foresporsel.uuid.toString(),
        )
    }

    companion object {
        private const val DEFAULT_FAILED_JP_ID = 0
        private val log = LoggerFactory.getLogger(JournalforingService::class.java)
    }
}
