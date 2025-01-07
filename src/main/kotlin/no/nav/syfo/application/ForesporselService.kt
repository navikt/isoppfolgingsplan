package no.nav.syfo.application

import no.nav.syfo.domain.Foresporsel
import no.nav.syfo.domain.Personident

class ForesporselService(
    private val varselProducer: IVarselProducer,
    private val repository: IForesporselRepository,
) {
    fun storeAndSendToNarmesteleder(foresporsel: Foresporsel): Result<Foresporsel> {
        val storedForesporsel = repository.createForesporsel(foresporsel)
        return varselProducer.sendNarmesteLederVarsel(
            foresporsel = storedForesporsel,
        )
        // TODO: Set published in db
    }

    fun getForesporsler(personident: Personident): List<Foresporsel> {
        return repository.getForesporsler(personident)
    }
}
