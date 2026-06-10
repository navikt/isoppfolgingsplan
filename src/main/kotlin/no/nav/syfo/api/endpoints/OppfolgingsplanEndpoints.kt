package no.nav.syfo.api.endpoints

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.api.model.ForesporselRequestDTO
import no.nav.syfo.api.model.ForesporselResponseDTO
import no.nav.syfo.application.ForesporselService
import no.nav.syfo.common.tilgangskontroll.checkPersonAndSyfoTilgang
import no.nav.syfo.domain.Personident
import no.nav.syfo.domain.Veilederident
import no.nav.syfo.domain.Virksomhetsnummer
import no.nav.syfo.common.tilgangskontroll.client.TilgangskontrollClient
import no.nav.syfo.common.types.ident.PersonIdent

fun Route.registerOppfolgingsplanEndpoints(
    tilgangskontrollClient: TilgangskontrollClient,
    foresporselService: ForesporselService,
) {
    route("/api/internad/v1/oppfolgingsplan") {
        get("/foresporsler") {
            checkPersonAndSyfoTilgang(
                action = "GET /foresporsler",
                tilgangskontrollClient = tilgangskontrollClient,
            ) { _, targetPersonIdent, _ ->
                val personident = Personident(targetPersonIdent.value)

                val foresporsler = foresporselService.getForesporsler(personident)
                val responseDTO = foresporsler.map { ForesporselResponseDTO.fromForesporsel(it) }

                call.respond(HttpStatusCode.OK, responseDTO)
            }
        }

        post("/foresporsler") {
            val requestDTO = call.receive<ForesporselRequestDTO>()
            val personidentFromBody = requestDTO.arbeidstakerPersonident

            checkPersonAndSyfoTilgang(
                action = "POST /foresporsler",
                personIdent = PersonIdent(personidentFromBody),
                tilgangskontrollClient = tilgangskontrollClient,
                requiresWriteAccess = true,
            ) { authorizedUser, targetPersonIdent, _ ->
                val result =
                    foresporselService.createForesporsel(
                        arbeidstakerPersonident = Personident(targetPersonIdent.value),
                        veilederident = Veilederident(authorizedUser.navIdent.value),
                        virksomhetsnummer = Virksomhetsnummer(requestDTO.virksomhetsnummer),
                        narmestelederPersonident = Personident(requestDTO.narmestelederPersonident),
                        document = requestDTO.document,
                    )
                call.respond(HttpStatusCode.Created, ForesporselResponseDTO.fromForesporsel(result))
            }
        }
    }
}
