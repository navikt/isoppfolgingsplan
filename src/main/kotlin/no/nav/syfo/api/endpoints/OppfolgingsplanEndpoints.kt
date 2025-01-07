package no.nav.syfo.api.endpoints

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.api.model.ForesporselRequestDTO
import no.nav.syfo.api.model.ForesporselResponseDTO
import no.nav.syfo.application.ForesporselService
import no.nav.syfo.domain.Foresporsel
import no.nav.syfo.domain.Personident
import no.nav.syfo.domain.Veilederident
import no.nav.syfo.domain.Virksomhetsnummer
import no.nav.syfo.infrastructure.NAV_PERSONIDENT_HEADER
import no.nav.syfo.infrastructure.clients.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.infrastructure.clients.veiledertilgang.validateVeilederAccess
import no.nav.syfo.util.getPersonident

fun Route.registerOppfolgingsplanEndpoints(
    veilederTilgangskontrollClient: VeilederTilgangskontrollClient,
    foresporselService: ForesporselService,
) {
    route("/api/internad/v1/oppfolgingsplan") {
        get("/foresporsler") {
            val personident =
                call.getPersonident()
                    ?: throw IllegalArgumentException(
                        "Failed to access foresporsel for person: No $NAV_PERSONIDENT_HEADER supplied in request header"
                    )

            validateVeilederAccess(
                action = "GET /foresporsler",
                personident = personident,
                veilederTilgangskontrollClient = veilederTilgangskontrollClient,
            ) {
                val foresporsler =
                    foresporselService.getForesporsler(
                        personident = personident,
                    )
                val responseDTO = foresporsler.map { ForesporselResponseDTO.fromForesporsel(it) }
                if (responseDTO.isEmpty()) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.OK, responseDTO)
                }
            }
        }

        post("/foresporsler") {
            val requestDTO = call.receive<ForesporselRequestDTO>()

            validateVeilederAccess(
                action = "POST /foresporsler",
                personident = Personident(requestDTO.arbeidstakerPersonident),
                veilederTilgangskontrollClient = veilederTilgangskontrollClient,
            ) {
                val foresporsel =
                    Foresporsel(
                        arbeidstakerPersonident = Personident(requestDTO.arbeidstakerPersonident),
                        veilederident = Veilederident(requestDTO.veilederident),
                        virksomhetsnummer = Virksomhetsnummer(requestDTO.virksomhetsnummer),
                        narmestelederPersonident = Personident(requestDTO.narmestelederPersonident),
                    )
                val result = foresporselService.storeAndSendToNarmesteleder(foresporsel)

                if (result.isSuccess) {
                    call.respond(HttpStatusCode.Created, ForesporselResponseDTO.fromForesporsel(result.getOrNull()!!))
                } else {
                    call.respond(HttpStatusCode.InternalServerError, result.toString())
                }
            }
        }
    }
}
