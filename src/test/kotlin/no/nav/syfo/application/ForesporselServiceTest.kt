package no.nav.syfo.application

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.syfo.ExternalMockEnvironment
import no.nav.syfo.UserConstants
import no.nav.syfo.generator.generateDocumentComponent
import no.nav.syfo.generator.generateForsporsel
import no.nav.syfo.infrastructure.database.dropData
import no.nav.syfo.infrastructure.database.repository.ForesporselRepository
import no.nav.syfo.infrastructure.journalforing.JournalforingService
import no.nav.syfo.infrastructure.kafka.VarselProducer
import no.nav.syfo.infrastructure.kafka.esyfovarsel.EsyfovarselHendelse
import no.nav.syfo.infrastructure.kafka.esyfovarsel.EsyfovarselHendelseProducer
import no.nav.syfo.infrastructure.kafka.esyfovarsel.HendelseType
import no.nav.syfo.infrastructure.kafka.esyfovarsel.NarmesteLederHendelse
import no.nav.syfo.shouldBeEqualTo
import no.nav.syfo.shouldNotBeEqualTo
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.junit.jupiter.api.BeforeEach
import java.util.concurrent.Future
import kotlin.test.Test
import kotlin.test.assertTrue

class ForesporselServiceTest {
    private val externalMockEnvironment = ExternalMockEnvironment.instance
    private val database = externalMockEnvironment.database
    private val kafkaProducerMock = mockk<KafkaProducer<String, EsyfovarselHendelse>>()
    private val varselProducer =
        VarselProducer(narmesteLederVarselProducer = EsyfovarselHendelseProducer(kafkaProducerMock))
    private val journalforingService =
        JournalforingService(
            dokarkivClient = externalMockEnvironment.dokarkivClient,
            eregClient = externalMockEnvironment.eregClient,
            pdfClient = externalMockEnvironment.pdfClient,
            isJournalforingRetryEnabled = externalMockEnvironment.environment.isJournalforingRetryEnabled,
        )
    private val foresporselRepository = ForesporselRepository(database)
    private val foresporselService =
        ForesporselService(
            varselProducer = varselProducer,
            repository = foresporselRepository,
            journalforingService = journalforingService,
        )

    @BeforeEach
    fun setup() {
        clearAllMocks()
        coEvery { kafkaProducerMock.send(any()) } returns mockk<Future<RecordMetadata>>(relaxed = true)
        database.dropData()
    }

    @Test
    fun `createForesporsel stores`() {
        val foresporsel =
            foresporselService.createForesporsel(
                arbeidstakerPersonident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                veilederident = UserConstants.VEILEDER_IDENT,
                virksomhetsnummer = UserConstants.VIRKSOMHETSNUMMER,
                narmestelederPersonident = UserConstants.NARMESTELEDER_FNR,
                document = generateDocumentComponent(),
            )

        val stored = foresporselService.getForesporsler(UserConstants.ARBEIDSTAKER_PERSONIDENT)
        stored.size shouldBeEqualTo 1
        val storedForesporsel = stored[0]
        storedForesporsel.uuid shouldBeEqualTo foresporsel.uuid
        storedForesporsel.journalpostId shouldBeEqualTo null
        storedForesporsel.document shouldBeEqualTo foresporsel.document
    }

    @Test
    fun journalforing() {
        val foresporsel = foresporselRepository.createForesporsel(generateForsporsel())

        runBlocking {
            foresporselService.journalforForesporsler()
        }

        val stored = foresporselService.getForesporsler(UserConstants.ARBEIDSTAKER_PERSONIDENT)
        stored.size shouldBeEqualTo 1
        val storedForesporsel = stored[0]
        storedForesporsel.uuid shouldBeEqualTo foresporsel.uuid
        storedForesporsel.journalpostId shouldNotBeEqualTo null
    }

    @Test
    fun `sends unpublished foresporsel to narmeste leder`() {
        foresporselRepository.createForesporsel(generateForsporsel())

        val results = foresporselService.publishedNarmestelederVarsler()
        results.size shouldBeEqualTo 1
        assertTrue(results.any { it.isSuccess })

        val producerRecordSlot = slot<ProducerRecord<String, EsyfovarselHendelse>>()
        verify(exactly = 1) { kafkaProducerMock.send(capture(producerRecordSlot)) }

        val esyfovarselHendelse = producerRecordSlot.captured.value() as NarmesteLederHendelse
        esyfovarselHendelse.arbeidstakerFnr shouldBeEqualTo UserConstants.ARBEIDSTAKER_PERSONIDENT.value
        esyfovarselHendelse.orgnummer shouldBeEqualTo UserConstants.VIRKSOMHETSNUMMER.value
        esyfovarselHendelse.narmesteLederFnr shouldBeEqualTo UserConstants.NARMESTELEDER_FNR.value
        esyfovarselHendelse.type shouldBeEqualTo HendelseType.NL_OPPFOLGINGSPLAN_FORESPORSEL

        assertTrue(foresporselService.publishedNarmestelederVarsler().isEmpty())
    }

    @Test
    fun `does not send published foresporsel to narmeste leder`() {
        val foresporsel = foresporselRepository.createForesporsel(generateForsporsel())
        foresporselRepository.setPublishedAt(foresporsel.uuid)

        val results = foresporselService.publishedNarmestelederVarsler()
        assertTrue(results.isEmpty())
    }

    @Test
    fun `send to narmeste leder fails when kafka producer fails`() {
        coEvery { kafkaProducerMock.send(any()) } throws Exception("Kafka error")
        foresporselRepository.createForesporsel(generateForsporsel())

        val results = foresporselService.publishedNarmestelederVarsler()
        results.size shouldBeEqualTo 1
        assertTrue(results.any { it.isFailure })
    }
}
