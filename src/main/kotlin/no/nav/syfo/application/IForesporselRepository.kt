package no.nav.syfo.application

import no.nav.syfo.domain.Foresporsel
import no.nav.syfo.domain.Personident

interface IForesporselRepository {
    fun createForesporsel(foresporsel: Foresporsel): Foresporsel

    fun getForesporsler(personident: Personident): List<Foresporsel>
}
