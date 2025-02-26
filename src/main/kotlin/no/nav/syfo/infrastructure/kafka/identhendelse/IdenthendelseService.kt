package no.nav.syfo.infrastructure.kafka.identhendelse

import no.nav.syfo.application.IForesporselRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class IdenthendelseService(private val repository: IForesporselRepository) {
    fun handle(identhendelse: KafkaIdenthendelseDTO) {
        val (aktivIdent, inaktiveIdenter) = identhendelse.getFolkeregisterIdenter()
        if (aktivIdent != null) {
            val foresporsler = inaktiveIdenter.flatMap { repository.getForesporsler(it) }

            if (foresporsler.isNotEmpty()) {
                repository.updateArbeidstakerPersonident(
                    nyPersonident = aktivIdent,
                    foresporsler = foresporsler,
                )
                log.info("Identhendelse: Updated ${foresporsler.size} foresporsler based on Identhendelse from PDL")
            }
        } else {
            log.warn("Identhendelse ignored - Mangler aktiv ident i PDL")
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
