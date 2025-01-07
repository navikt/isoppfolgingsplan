package no.nav.syfo.application

import no.nav.syfo.domain.Foresporsel
import no.nav.syfo.domain.Personident
import no.nav.syfo.domain.Veilederident
import no.nav.syfo.domain.Virksomhetsnummer

class ForesporselService(
    private val varselProducer: IVarselProducer,
    private val repository: IForesporselRepository,
) {
    fun createForesporsel(
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
        return varselProducer.sendNarmesteLederVarsel(
            foresporsel = storedForesporsel,
        )
        // TODO: Set published in db
    }

    fun getForesporsler(personident: Personident): List<Foresporsel> {
        return repository.getForesporsler(personident)
    }
}
