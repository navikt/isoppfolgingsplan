package no.nav.syfo.application

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.syfo.ExternalMockEnvironment
import no.nav.syfo.UserConstants
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
    val externalMockEnvironment = ExternalMockEnvironment.instance
    val database = externalMockEnvironment.database
    val kafkaProducerMock = mockk<KafkaProducer<String, EsyfovarselHendelse>>()
    val varselProducer = VarselProducer(narmesteLederVarselProducer = EsyfovarselHendelseProducer(kafkaProducerMock))
    val journalforingService =
        JournalforingService(
            dokarkivClient = externalMockEnvironment.dokarkivClient,
            eregClient = externalMockEnvironment.eregClient,
            isJournalforingRetryEnabled = externalMockEnvironment.environment.isJournalforingRetryEnabled,
        )
    val foresporselService =
        ForesporselService(
            varselProducer = varselProducer,
            repository = ForesporselRepository(database),
            journalforingService = journalforingService,
        )

    @BeforeEach
    fun setup() {
        clearAllMocks()
        coEvery { kafkaProducerMock.send(any()) } returns mockk<Future<RecordMetadata>>(relaxed = true)
        database.dropData()
    }

    @Test
    fun `store and send to narmeste leder produces to kafka`() {
        val result =
            runBlocking {
                foresporselService.createForesporsel(
                    arbeidstakerPersonident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                    veilederident = UserConstants.VEILEDER_IDENT,
                    virksomhetsnummer = UserConstants.VIRKSOMHETSNUMMER,
                    narmestelederPersonident = UserConstants.NARMESTELEDER_FNR,
                )
            }

        assertTrue(result.isSuccess)

        val producerRecordSlot = slot<ProducerRecord<String, EsyfovarselHendelse>>()
        verify(exactly = 1) { kafkaProducerMock.send(capture(producerRecordSlot)) }

        val esyfovarselHendelse = producerRecordSlot.captured.value() as NarmesteLederHendelse
        esyfovarselHendelse.arbeidstakerFnr shouldBeEqualTo UserConstants.ARBEIDSTAKER_PERSONIDENT.value
        esyfovarselHendelse.orgnummer shouldBeEqualTo UserConstants.VIRKSOMHETSNUMMER.value
        esyfovarselHendelse.narmesteLederFnr shouldBeEqualTo UserConstants.NARMESTELEDER_FNR.value
        esyfovarselHendelse.type shouldBeEqualTo HendelseType.NL_OPPFOLGINGSPLAN_FORESPORSEL
    }

    @Test
    fun `store and send to narmeste leder stores and does journalforing`() {
        val result =
            runBlocking {
                foresporselService.createForesporsel(
                    arbeidstakerPersonident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                    veilederident = UserConstants.VEILEDER_IDENT,
                    virksomhetsnummer = UserConstants.VIRKSOMHETSNUMMER,
                    narmestelederPersonident = UserConstants.NARMESTELEDER_FNR,
                )
            }
        assertTrue(result.isSuccess)
        val stored = foresporselService.getForesporsler(UserConstants.ARBEIDSTAKER_PERSONIDENT)
        stored.size shouldBeEqualTo 1
        val storedForesporsel = stored[0]
        storedForesporsel.uuid shouldBeEqualTo result.getOrNull()?.uuid
        storedForesporsel.journalpostId shouldNotBeEqualTo null
    }

    @Test
    fun `journalforing`() {
        val result =
            runBlocking {
                foresporselService.createForesporsel(
                    arbeidstakerPersonident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                    veilederident = UserConstants.VEILEDER_IDENT,
                    virksomhetsnummer = UserConstants.VIRKSOMHETSNUMMER,
                    narmestelederPersonident = UserConstants.NARMESTELEDER_FNR,
                )
            }
        assertTrue(result.isSuccess)
        runBlocking {
            foresporselService.journalforForesporsler()
        }
        val stored = foresporselService.getForesporsler(UserConstants.ARBEIDSTAKER_PERSONIDENT)
        stored.size shouldBeEqualTo 1
        val storedForesporsel = stored[0]
        storedForesporsel.uuid shouldBeEqualTo result.getOrNull()?.uuid
        storedForesporsel.journalpostId shouldNotBeEqualTo null
    }

    @Test
    fun `send to narmeste leder fails when kafka producer fails`() {
        coEvery { kafkaProducerMock.send(any()) } throws Exception("Kafka error")

        val result =
            runBlocking {
                foresporselService.createForesporsel(
                    arbeidstakerPersonident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                    veilederident = UserConstants.VEILEDER_IDENT,
                    virksomhetsnummer = UserConstants.VIRKSOMHETSNUMMER,
                    narmestelederPersonident = UserConstants.NARMESTELEDER_FNR,
                )
            }
        assertTrue(result.isFailure)
    }
}
