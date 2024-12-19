package no.nav.syfo.application

import no.nav.syfo.domain.Foresporsel

class ForesporselService(private val varselProducer: IVarselProducer) {
    fun sendToNarmesteleder(foresporsel: Foresporsel): Result<Foresporsel> {
        return varselProducer.sendNarmesteLederVarsel(
            foresporsel = foresporsel,
        )
        // TODO: Set published in db
    }
}
