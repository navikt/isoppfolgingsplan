package no.nav.syfo.api.endpoints

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.api.model.ForesporselRequestDTO
import no.nav.syfo.api.model.ForesporselResponseDTO
import no.nav.syfo.application.ForesporselService
import no.nav.syfo.domain.Personident
import no.nav.syfo.domain.Veilederident
import no.nav.syfo.domain.Virksomhetsnummer
import no.nav.syfo.infrastructure.NAV_PERSONIDENT_HEADER
import no.nav.syfo.tilgangskontroll.client.VeilederTilgangskontrollClient
import no.nav.syfo.tilgangskontroll.ktor.checkVeilederTilgang
import no.nav.syfo.tilgangskontroll.ktor.getNAVIdent
import no.nav.syfo.tilgangskontroll.ktor.getPersonident

fun Route.registerOppfolgingsplanEndpoints(
    veilederTilgangskontrollClient: VeilederTilgangskontrollClient,
    foresporselService: ForesporselService,
) {
    route("/api/internad/v1/oppfolgingsplan") {
        get("/foresporsler") {
            val personidentString =
                call.getPersonident()
                    ?: throw IllegalArgumentException(
                        "Failed to access foresporsel for person: No $NAV_PERSONIDENT_HEADER supplied in request header"
                    )

            checkVeilederTilgang(
                action = "GET /foresporsler",
                personident = personidentString,
                veilederTilgangskontrollClient = veilederTilgangskontrollClient,
            ) {
                val foresporsler =
                    foresporselService.getForesporsler(
                        personident = Personident(personidentString),
                    )
                val responseDTO = foresporsler.map { ForesporselResponseDTO.fromForesporsel(it) }
                call.respond(HttpStatusCode.OK, responseDTO)
            }
        }

        post("/foresporsler") {
            val requestDTO = call.receive<ForesporselRequestDTO>()

            checkVeilederTilgang(
                action = "POST /foresporsler",
                personident = requestDTO.arbeidstakerPersonident,
                veilederTilgangskontrollClient = veilederTilgangskontrollClient,
                requiresWriteAccess = true,
            ) {
                val result =
                    foresporselService.createForesporsel(
                        arbeidstakerPersonident = Personident(requestDTO.arbeidstakerPersonident),
                        veilederident = Veilederident(call.getNAVIdent()),
                        virksomhetsnummer = Virksomhetsnummer(requestDTO.virksomhetsnummer),
                        narmestelederPersonident = Personident(requestDTO.narmestelederPersonident),
                        document = requestDTO.document,
                    )
                call.respond(HttpStatusCode.Created, ForesporselResponseDTO.fromForesporsel(result))
            }
        }
    }
}
