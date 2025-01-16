package no.nav.syfo.infrastructure.cronjob

import no.nav.syfo.application.ForesporselService

class JournalforForesporselCronjob(
    private val foresporselService: ForesporselService,
) : Cronjob {
    override val initialDelayMinutes: Long = 3
    override val intervalDelayMinutes: Long = 1

    override suspend fun run(): List<Result<Any>> = foresporselService.journalforForesporsler()
}
