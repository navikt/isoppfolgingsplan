package no.nav.syfo.api.endpoints

import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.infrastructure.metric.METRICS_REGISTRY

fun Routing.metricEndpoints() {
    get("/internal/metrics") {
        call.respondText(METRICS_REGISTRY.scrape())
    }
}
