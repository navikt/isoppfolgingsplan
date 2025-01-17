package no.nav.syfo.infrastructure.cronjob

import no.nav.syfo.application.ForesporselService

class PublishNarmestelederVarselCronjob(
    private val foresporselService: ForesporselService,
) : Cronjob {
    override val initialDelayMinutes: Long = 5
    override val intervalDelayMinutes: Long = 10

    override suspend fun run(): List<Result<Any>> = foresporselService.publishedNarmestelederVarsler()
}
