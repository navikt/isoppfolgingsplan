package no.nav.syfo.application

import io.mockk.*
import no.nav.syfo.ExternalMockEnvironment
import no.nav.syfo.UserConstants
import no.nav.syfo.generator.generateForsporsel
import no.nav.syfo.infrastructure.database.dropData
import no.nav.syfo.infrastructure.database.repository.ForesporselRepository
import no.nav.syfo.infrastructure.kafka.VarselProducer
import no.nav.syfo.infrastructure.kafka.esyfovarsel.EsyfovarselHendelse
import no.nav.syfo.infrastructure.kafka.esyfovarsel.EsyfovarselHendelseProducer
import no.nav.syfo.infrastructure.kafka.esyfovarsel.HendelseType
import no.nav.syfo.infrastructure.kafka.esyfovarsel.NarmesteLederHendelse
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.junit.jupiter.api.BeforeEach
import java.util.concurrent.Future
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ForesporselServiceTest {
    val database = ExternalMockEnvironment.instance.database
    private val kafkaProducerMock = mockk<KafkaProducer<String, EsyfovarselHendelse>>()
    private val varselProducer = VarselProducer(narmesteLederVarselProducer = EsyfovarselHendelseProducer(kafkaProducerMock))
    private val foresporselService =
        ForesporselService(
            varselProducer = varselProducer,
            repository = ForesporselRepository(database)
        )

    @BeforeEach
    fun setup() {
        clearAllMocks()
        coEvery { kafkaProducerMock.send(any()) } returns mockk<Future<RecordMetadata>>(relaxed = true)
        database.dropData()
    }

    @Test
    fun `store and send to narmeste leder produces to kafka`() {
        val foresporsel = generateForsporsel()
        val result =
            foresporselService.storeAndSendToNarmesteleder(
                foresporsel = foresporsel,
            )

        assertTrue(result.isSuccess)

        val producerRecordSlot = slot<ProducerRecord<String, EsyfovarselHendelse>>()
        verify(exactly = 1) { kafkaProducerMock.send(capture(producerRecordSlot)) }

        val esyfovarselHendelse = producerRecordSlot.captured.value() as NarmesteLederHendelse
        assertEquals(foresporsel.arbeidstakerPersonident.value, esyfovarselHendelse.arbeidstakerFnr)
        assertEquals(foresporsel.virksomhetsnummer.value, esyfovarselHendelse.orgnummer)
        assertEquals(foresporsel.narmestelederPersonident.value, esyfovarselHendelse.narmesteLederFnr)
        assertEquals(HendelseType.NL_OPPFOLGINGSPLAN_FORESPORSEL, esyfovarselHendelse.type)
    }

    @Test
    fun `store and send to narmeste leder stores`() {
        val foresporsel = generateForsporsel()
        val result =
            foresporselService.storeAndSendToNarmesteleder(
                foresporsel = foresporsel,
            )
        assertTrue(result.isSuccess)
        val stored = foresporselService.getForesporsler(UserConstants.ARBEIDSTAKER_PERSONIDENT)
        assertTrue(stored.size == 1)
        val storedForesporsel = stored[0]
        assertTrue(storedForesporsel.uuid == foresporsel.uuid)
    }

    @Test
    fun `send to narmeste leder fails when kafka producer fails`() {
        val foresporsel = generateForsporsel()
        coEvery { kafkaProducerMock.send(any()) } throws Exception("Kafka error")

        val result =
            foresporselService.storeAndSendToNarmesteleder(
                foresporsel = foresporsel,
            )

        assertTrue(result.isFailure)
    }
}
