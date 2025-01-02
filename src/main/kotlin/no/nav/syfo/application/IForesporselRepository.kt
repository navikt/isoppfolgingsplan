package no.nav.syfo.application

import no.nav.syfo.domain.Foresporsel
import java.util.*

interface IForesporselRepository {
    fun createForesporsel(foresporsel: Foresporsel): Foresporsel

    fun getForesporsel(foresporselUuid: UUID): Foresporsel?
}
