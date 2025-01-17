package no.nav.syfo.application

import no.nav.syfo.domain.Foresporsel
import no.nav.syfo.domain.Personident
import no.nav.syfo.domain.Veilederident
import no.nav.syfo.domain.Virksomhetsnummer
import no.nav.syfo.domain.DocumentComponent

class ForesporselService(
    private val varselProducer: IVarselProducer,
    private val repository: IForesporselRepository,
    private val journalforingService: IJournalforingService,
) {
    fun createForesporsel(
        arbeidstakerPersonident: Personident,
        veilederident: Veilederident,
        virksomhetsnummer: Virksomhetsnummer,
        narmestelederPersonident: Personident,
        document: List<DocumentComponent>,
    ): Foresporsel {
        val foresporsel =
            Foresporsel(
                arbeidstakerPersonident = arbeidstakerPersonident,
                veilederident = veilederident,
                virksomhetsnummer = virksomhetsnummer,
                narmestelederPersonident = narmestelederPersonident,
                document = document,
            )

        val storedForesporsel = repository.createForesporsel(foresporsel)

        return storedForesporsel
    }

    fun getForesporsler(personident: Personident): List<Foresporsel> {
        return repository.getForesporsler(personident)
    }

    suspend fun journalforForesporsler(): List<Result<Foresporsel>> =
        repository.getForesporslerForJournalforing().map { foresporsel ->
            journalforingService.journalfor(
                foresporsel = foresporsel,
            ).map { journalpostId ->
                val journalfortForesporsel = foresporsel.journalfor(journalpostId = journalpostId)
                repository.setJournalpostId(journalfortForesporsel)
                journalfortForesporsel
            }
        }

    fun publishedNarmestelederVarsler(): List<Result<Foresporsel>> {
        val unpublishedForesporsler = repository.getUnpublishedForesporsler()

        return unpublishedForesporsler.map { foresporsel ->
            varselProducer.sendNarmesteLederVarsel(foresporsel).map {
                repository.setPublishedAt(it.uuid)
                it
            }
        }
    }
}
