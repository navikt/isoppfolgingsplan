package no.nav.syfo.application

import no.nav.syfo.domain.Foresporsel
import no.nav.syfo.domain.Personident
import java.util.UUID

interface IForesporselRepository {
    fun createForesporsel(foresporsel: Foresporsel): Foresporsel

    fun getForesporsler(personident: Personident): List<Foresporsel>

    fun getForesporslerForJournalforing(): List<Foresporsel>

    fun setJournalpostId(journalfortForesporsel: Foresporsel)

    fun setPublishedAt(foresporselUuid: UUID)
}
