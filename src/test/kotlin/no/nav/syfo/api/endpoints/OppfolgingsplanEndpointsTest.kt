package no.nav.syfo.api.endpoints

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.testing.*
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.slot
import no.nav.syfo.ExternalMockEnvironment
import no.nav.syfo.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.UserConstants.ARBEIDSTAKER_PERSONIDENT_VEILEDER_NO_ACCESS
import no.nav.syfo.UserConstants.NARMESTELEDER_FNR
import no.nav.syfo.UserConstants.VEILEDER_IDENT
import no.nav.syfo.UserConstants.VIRKSOMHETSNUMMER
import no.nav.syfo.api.generateJWT
import no.nav.syfo.api.model.*
import no.nav.syfo.api.testApiModule
import no.nav.syfo.domain.Foresporsel
import no.nav.syfo.domain.Personident
import no.nav.syfo.infrastructure.NAV_PERSONIDENT_HEADER
import no.nav.syfo.infrastructure.database.dropData
import no.nav.syfo.util.configure
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

object OppfolgingsplanEndpointsTest {
    val urlOppfolgingsplan = "/api/internad/v1/oppfolgingsplan"
    val externalMockEnvironment = ExternalMockEnvironment.instance
    val database = externalMockEnvironment.database
    val varselProducer = externalMockEnvironment.varselProducer

    fun ApplicationTestBuilder.setupApiAndClient(): HttpClient {
        application {
            testApiModule(
                externalMockEnvironment = ExternalMockEnvironment.instance,
            )
        }
        val client =
            createClient {
                install(ContentNegotiation) {
                    jackson { configure() }
                }
            }
        return client
    }

    val validToken =
        generateJWT(
            audience = externalMockEnvironment.environment.azure.appClientId,
            issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
            navIdent = VEILEDER_IDENT.value,
        )

    val personIdent = ARBEIDSTAKER_PERSONIDENT.value
    val personIdentNoAccess = ARBEIDSTAKER_PERSONIDENT_VEILEDER_NO_ACCESS.value

    @BeforeEach
    fun beforeEachTest() {
        database.dropData()
        clearMocks(varselProducer)
        val foresporselSlot = slot<Foresporsel>()
        every { varselProducer.sendNarmesteLederVarsel(capture(foresporselSlot)) }.answers {
            Result.success(foresporselSlot.captured)
        }
    }

    @Test
    fun `Successfully creates a new foresporsel with varsel`() {
        testApplication {
            val client = setupApiAndClient()

            val foresporselRequestDTO = createForesporselRequestDTO()
            val response =
                client.post("$urlOppfolgingsplan/foresporsler") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(validToken)
                    setBody(foresporselRequestDTO)
                }
            assertEquals(HttpStatusCode.Created, response.status)
            coVerify(exactly = 1) {
                varselProducer.sendNarmesteLederVarsel(any())
            }
        }
    }

    @Test
    fun `Successfully stores and gets foresporsel`() {
        testApplication {
            val client = setupApiAndClient()

            val foresporselRequestDTO = createForesporselRequestDTO()
            val responseCreate =
                client.post("$urlOppfolgingsplan/foresporsler") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(validToken)
                    setBody(foresporselRequestDTO)
                }
            assertEquals(HttpStatusCode.Created, responseCreate.status)

            val response =
                client.get("$urlOppfolgingsplan/foresporsler") {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                }
            assertEquals(HttpStatusCode.OK, response.status)
            val stored = response.body<List<ForesporselResponseDTO>>()
            assertEquals(stored.size, 1)
            val storedForesporsel = stored[0]
            assertEquals(storedForesporsel.arbeidstakerPersonident, foresporselRequestDTO.arbeidstakerPersonident)
            assertEquals(storedForesporsel.veilederident, foresporselRequestDTO.veilederident)
            assertEquals(storedForesporsel.narmestelederPersonident, foresporselRequestDTO.narmestelederPersonident)
            assertEquals(storedForesporsel.virksomhetsnummer, foresporselRequestDTO.virksomhetsnummer)
        }
    }

    @Test
    fun `Returns status Unauthorized if no token is supplied`() {
        testApplication {
            val client = setupApiAndClient()
            val response = client.post("$urlOppfolgingsplan/foresporsler")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    @Test
    fun `Returns status Forbidden if denied access to person`() {
        testApplication {
            val client = setupApiAndClient()
            val foresporselRequestDTO = createForesporselRequestDTO(ARBEIDSTAKER_PERSONIDENT_VEILEDER_NO_ACCESS)
            val response =
                client.post("$urlOppfolgingsplan/foresporsler") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(validToken)
                    setBody(foresporselRequestDTO)
                }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }
    }

    private fun createForesporselRequestDTO(arbeidstakerPersonident: Personident = ARBEIDSTAKER_PERSONIDENT) =
        ForesporselRequestDTO(
            arbeidstakerPersonident = arbeidstakerPersonident.value,
            veilederident = VEILEDER_IDENT.value,
            virksomhetsnummer = VIRKSOMHETSNUMMER.value,
            narmestelederPersonident = NARMESTELEDER_FNR.value,
        )
}
