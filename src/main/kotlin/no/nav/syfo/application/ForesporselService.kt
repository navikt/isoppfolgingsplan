package no.nav.syfo.application

import no.nav.syfo.domain.Foresporsel
import no.nav.syfo.domain.Personident
import no.nav.syfo.domain.Veilederident
import no.nav.syfo.domain.Virksomhetsnummer
import org.slf4j.LoggerFactory

class ForesporselService(
    private val varselProducer: IVarselProducer,
    private val repository: IForesporselRepository,
    private val journalforingService: IJournalforingService,
) {
    suspend fun createForesporsel(
        arbeidstakerPersonident: Personident,
        veilederident: Veilederident,
        virksomhetsnummer: Virksomhetsnummer,
        narmestelederPersonident: Personident,
    ): Result<Foresporsel> {
        val foresporsel =
            Foresporsel(
                arbeidstakerPersonident = arbeidstakerPersonident,
                veilederident = veilederident,
                virksomhetsnummer = virksomhetsnummer,
                narmestelederPersonident = narmestelederPersonident,
            )

        val storedForesporsel = repository.createForesporsel(foresporsel)

        journalforForesporsel(storedForesporsel).onFailure {
            log.warn("Journalforing failed, cronjob will try again", it.cause)
        }
        return varselProducer.sendNarmesteLederVarsel(
            foresporsel = storedForesporsel,
        )
        // TODO: Set published in db
    }

    fun getForesporsler(personident: Personident): List<Foresporsel> {
        return repository.getForesporsler(personident)
    }

    suspend fun journalforForesporsler(): List<Result<Foresporsel>> =
        repository.getForesporslerForJournalforing().map { foresporsel ->
            journalforForesporsel(foresporsel)
        }

    private suspend fun journalforForesporsel(foresporsel: Foresporsel): Result<Foresporsel> =
        journalforingService.journalfor(
            foresporsel = foresporsel,
            // TODO: Generate PDF
            pdf = byteArrayOf(),
        ).map { journalpostId ->
            val journalfortForesporsel = foresporsel.journalfor(journalpostId = journalpostId)
            repository.setJournalpostId(journalfortForesporsel)
            journalfortForesporsel
        }

    companion object {
        private val log = LoggerFactory.getLogger(ForesporselService::class.java)
    }
}
