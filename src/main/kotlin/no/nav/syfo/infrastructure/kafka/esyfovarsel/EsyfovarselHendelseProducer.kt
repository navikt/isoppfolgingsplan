package no.nav.syfo.infrastructure.kafka.esyfovarsel

import no.nav.syfo.domain.Foresporsel
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import java.util.*

class EsyfovarselHendelseProducer(
    private val producer: KafkaProducer<String, EsyfovarselHendelse>,
) {
    fun sendNarmesteLederVarsel(foresporsel: Foresporsel): Result<Foresporsel> {
        return try {
            producer.send(
                ProducerRecord(
                    ESYFOVARSEL_TOPIC,
                    UUID.randomUUID().toString(),
                    NarmesteLederHendelse(
                        type = HendelseType.NL_OPPFOLGINGSPLAN_FORESPORSEL,
                        data = null,
                        narmesteLederFnr = foresporsel.narmestelederPersonident.value,
                        arbeidstakerFnr = foresporsel.arbeidstakerPersonident.value,
                        orgnummer = foresporsel.virksomhetsnummer.value,
                    ),
                )
            ).get()
            Result.success(foresporsel)
        } catch (e: Exception) {
            log.error("Exception was thrown when attempting to send varsel with uuid ${foresporsel.uuid} to esyfovarsel: ${e.message}")
            Result.failure(e)
        }
    }

    companion object {
        private const val ESYFOVARSEL_TOPIC = "team-esyfo.varselbus"
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
