package no.nav.syfo.infrastructure.cronjob

import no.nav.syfo.ApplicationState
import no.nav.syfo.Environment
import no.nav.syfo.application.ForesporselService
import no.nav.syfo.infrastructure.clients.leaderelection.LeaderPodClient
import no.nav.syfo.launchBackgroundTask

fun launchCronjobs(
    applicationState: ApplicationState,
    environment: Environment,
    foresporselService: ForesporselService,
) {
    val leaderPodClient = LeaderPodClient(electorPath = environment.electorPath)
    val cronjobRunner =
        CronjobRunner(
            applicationState = applicationState,
            leaderPodClient = leaderPodClient,
        )
    val cronjobs = mutableListOf<Cronjob>()
    cronjobs.add(
        JournalforForesporselCronjob(foresporselService = foresporselService)
    )

    val publishNarmestelederVarselCronjob = PublishNarmestelederVarselCronjob(foresporselService = foresporselService)
    cronjobs.add(publishNarmestelederVarselCronjob)

    cronjobs.forEach {
        launchBackgroundTask(
            applicationState = applicationState,
        ) {
            cronjobRunner.start(cronjob = it)
        }
    }
}
