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
import no.nav.syfo.common.tilgangskontroll.client.TilgangskontrollClient
import no.nav.syfo.common.tilgangskontroll.ktor.checkPersonAndSyfoTilgang
import no.nav.syfo.common.util.ktor.navIdent
import no.nav.syfo.common.util.ktor.personIdent

fun Route.registerOppfolgingsplanEndpoints(
    tilgangskontrollClient: TilgangskontrollClient,
    foresporselService: ForesporselService,
) {
    route("/api/internad/v1/oppfolgingsplan") {
        get("/foresporsler") {
            checkPersonAndSyfoTilgang(
                action = "GET /foresporsler",
                tilgangskontrollClient = tilgangskontrollClient,
            ) {
                val personident = Personident(call.personIdent)

                val foresporsler =
                    foresporselService.getForesporsler(
                        personident = personident,
                    )
                val responseDTO = foresporsler.map { ForesporselResponseDTO.fromForesporsel(it) }
                call.respond(HttpStatusCode.OK, responseDTO)
            }
        }

        post("/foresporsler") {
            val requestDTO = call.receive<ForesporselRequestDTO>()
            val personident = Personident(requestDTO.arbeidstakerPersonident)

            checkPersonAndSyfoTilgang(
                action = "POST /foresporsler",
                personIdent = personident.value,
                tilgangskontrollClient = tilgangskontrollClient,
                requiresWriteAccess = true,
            ) {
                val result =
                    foresporselService.createForesporsel(
                        arbeidstakerPersonident = personident,
                        veilederident = Veilederident(call.navIdent),
                        virksomhetsnummer = Virksomhetsnummer(requestDTO.virksomhetsnummer),
                        narmestelederPersonident = Personident(requestDTO.narmestelederPersonident),
                        document = requestDTO.document,
                    )
                call.respond(HttpStatusCode.Created, ForesporselResponseDTO.fromForesporsel(result))
            }
        }
    }
}
