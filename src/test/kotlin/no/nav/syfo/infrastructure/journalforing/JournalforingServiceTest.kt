package no.nav.syfo.infrastructure.journalforing

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.syfo.ExternalMockEnvironment
import no.nav.syfo.UserConstants
import no.nav.syfo.domain.Personident
import no.nav.syfo.domain.Virksomhetsnummer
import no.nav.syfo.generator.generateForsporsel
import no.nav.syfo.infrastructure.clients.dokarkiv.DokarkivClient
import no.nav.syfo.infrastructure.clients.dokarkiv.dto.*
import no.nav.syfo.infrastructure.mock.dokarkivResponse
import no.nav.syfo.infrastructure.mock.mockedJournalpostId
import no.nav.syfo.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class JournalforingServiceTest {
    val externalMockEnvironment = ExternalMockEnvironment.instance
    val dokarkivMock = mockk<DokarkivClient>(relaxed = true)
    val journalforingService =
        JournalforingService(
            dokarkivClient = dokarkivMock,
            eregClient = externalMockEnvironment.eregClient,
            isJournalforingRetryEnabled = externalMockEnvironment.environment.isJournalforingRetryEnabled,
        )

    @BeforeEach
    fun before() {
        clearAllMocks()
        coEvery { dokarkivMock.journalfor(any()) } returns dokarkivResponse
    }

    @Test
    fun `sender forventet journalpost til dokarkiv`() {
        val foresporsel = generateForsporsel()
        val journalpostId =
            runBlocking {
                journalforingService.journalfor(
                    foresporsel = foresporsel,
                    pdf = UserConstants.PDF_FORESPORSEL,
                )
            }.getOrThrow()

        journalpostId shouldBeEqualTo mockedJournalpostId

        coVerify(exactly = 1) {
            dokarkivMock.journalfor(
                journalpostRequest =
                    generateJournalpostRequest(
                        tittel = "Forespørsel til arbeidsgiver om oppfølgingsplan",
                        brevkodeType = BrevkodeType.FORESPORSEL_OPPFOLGINGSPLAN,
                        pdf = UserConstants.PDF_FORESPORSEL,
                        eksternReferanse = foresporsel.uuid,
                        mottakerVirksomhetsnummer = UserConstants.VIRKSOMHETSNUMMER,
                        mottakerNavn = UserConstants.VIRKSOMHETSNAVN,
                        brukerPersonident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                    )
            )
        }
    }

    @Test
    fun `feiler når kall til ereg feiler`() {
        val failingForesporsel = generateForsporsel(virksomhetsnummer = UserConstants.VIRKSOMHETSNUMMER_NO_VIRKSOMHETSNAVN)
        val result =
            runBlocking {
                journalforingService.journalfor(
                    foresporsel = failingForesporsel,
                    pdf = UserConstants.PDF_FORESPORSEL,
                )
            }
        result.isFailure shouldBeEqualTo true
        coVerify(exactly = 0) { dokarkivMock.journalfor(any()) }
    }
}

fun generateJournalpostRequest(
    tittel: String,
    brevkodeType: BrevkodeType,
    pdf: ByteArray,
    eksternReferanse: UUID,
    mottakerVirksomhetsnummer: Virksomhetsnummer,
    mottakerNavn: String,
    brukerPersonident: Personident,
    kanal: JournalpostKanal? = null,
    overstyrInnsynsregler: OverstyrInnsynsregler? = null,
) = JournalpostRequest(
    avsenderMottaker =
        AvsenderMottaker.create(
            id = mottakerVirksomhetsnummer.value,
            idType = BrukerIdType.VIRKSOMHETSNUMMER,
            navn = mottakerNavn,
        ),
    bruker =
        Bruker.create(
            id = brukerPersonident.value,
            idType = BrukerIdType.PERSON_IDENT
        ),
    tittel = tittel,
    dokumenter =
        listOf(
            Dokument.create(
                brevkode = brevkodeType,
                tittel = tittel,
                dokumentvarianter =
                    listOf(
                        Dokumentvariant.create(
                            filnavn = tittel,
                            filtype = FiltypeType.PDFA,
                            fysiskDokument = pdf,
                            variantformat = VariantformatType.ARKIV,
                        ),
                    ),
            ),
        ),
    kanal = kanal?.name,
    overstyrInnsynsregler = overstyrInnsynsregler?.name,
    journalpostType = JournalpostType.UTGAAENDE.name,
    eksternReferanseId = eksternReferanse.toString(),
)