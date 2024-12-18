package no.nav.syfo.infrastructure.kafka

import no.nav.syfo.application.IVarselProducer
import no.nav.syfo.domain.Foresporsel
import no.nav.syfo.infrastructure.kafka.esyfovarsel.EsyfovarselHendelseProducer

class VarselProducer(private val narmesteLederVarselProducer: EsyfovarselHendelseProducer) : IVarselProducer {
    override fun sendNarmesteLederVarsel(foresporsel: Foresporsel): Result<Foresporsel> =
        narmesteLederVarselProducer.sendNarmesteLederVarsel(foresporsel)
}
