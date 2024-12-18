package no.nav.syfo.application

import no.nav.syfo.domain.Foresporsel

interface IVarselProducer {
    fun sendNarmesteLederVarsel(foresporsel: Foresporsel): Result<Foresporsel>
}
