package no.nav.syfo.api.endpoints

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.testing.*
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.slot
import no.nav.syfo.ExternalMockEnvironment
import no.nav.syfo.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.UserConstants.ARBEIDSTAKER_PERSONIDENT_VEILEDER_NO_ACCESS
import no.nav.syfo.UserConstants.NARMESTELEDER_FNR
import no.nav.syfo.UserConstants.OTHER_NARMESTELEDER_FNR
import no.nav.syfo.UserConstants.OTHER_VIRKSOMHETSNUMMER
import no.nav.syfo.UserConstants.VEILEDER_IDENT
import no.nav.syfo.UserConstants.VIRKSOMHETSNUMMER
import no.nav.syfo.api.generateJWT
import no.nav.syfo.api.model.*
import no.nav.syfo.api.testApiModule
import no.nav.syfo.domain.Foresporsel
import no.nav.syfo.domain.Personident
import no.nav.syfo.generator.generateDocumentComponent
import no.nav.syfo.infrastructure.NAV_PERSONIDENT_HEADER
import no.nav.syfo.infrastructure.database.dropData
import no.nav.syfo.util.configure
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

object OppfolgingsplanEndpointsTest {
    private const val URL_OPPFOLGINGSPLAN = "/api/internad/v1/oppfolgingsplan"
    private val externalMockEnvironment = ExternalMockEnvironment.instance
    private val database = externalMockEnvironment.database
    private val varselProducer = externalMockEnvironment.varselProducer

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

    private val validToken =
        generateJWT(
            audience = externalMockEnvironment.environment.azure.appClientId,
            issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
            navIdent = VEILEDER_IDENT.value,
        )

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
    fun `Successfully stores and gets foresporsel`() {
        testApplication {
            val client = setupApiAndClient()

            val foresporselRequestDTO = createForesporselRequestDTO()
            val responseCreate =
                client.post("$URL_OPPFOLGINGSPLAN/foresporsler") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(validToken)
                    setBody(foresporselRequestDTO)
                }
            assertEquals(HttpStatusCode.Created, responseCreate.status)

            val response =
                client.get("$URL_OPPFOLGINGSPLAN/foresporsler") {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                }
            assertEquals(HttpStatusCode.OK, response.status)
            val stored = response.body<List<ForesporselResponseDTO>>()
            assertEquals(stored.size, 1)
            val storedForesporsel = stored[0]
            assertEquals(storedForesporsel.arbeidstakerPersonident, foresporselRequestDTO.arbeidstakerPersonident)
            assertEquals(storedForesporsel.narmestelederPersonident, foresporselRequestDTO.narmestelederPersonident)
            assertEquals(storedForesporsel.virksomhetsnummer, foresporselRequestDTO.virksomhetsnummer)
        }
    }

    @Test
    fun `Returns foresporsler ordered by created descending`() {
        testApplication {
            val client = setupApiAndClient()
            val foresporselRequestDTO = createForesporselRequestDTO()
            val otherForesporselRequestDTO =
                foresporselRequestDTO.copy(
                    virksomhetsnummer = OTHER_VIRKSOMHETSNUMMER.value,
                    narmestelederPersonident = OTHER_NARMESTELEDER_FNR.value,
                )

            client.post("$URL_OPPFOLGINGSPLAN/foresporsler") {
                contentType(ContentType.Application.Json)
                bearerAuth(validToken)
                setBody(foresporselRequestDTO)
            }.apply {
                assertEquals(HttpStatusCode.Created, status)
            }
            client.post("$URL_OPPFOLGINGSPLAN/foresporsler") {
                contentType(ContentType.Application.Json)
                bearerAuth(validToken)
                setBody(otherForesporselRequestDTO)
            }.apply {
                assertEquals(HttpStatusCode.Created, status)
            }

            val response =
                client.get("$URL_OPPFOLGINGSPLAN/foresporsler") {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val foresporsler = response.body<List<ForesporselResponseDTO>>()
            assertEquals(foresporsler.size, 2)

            val newestForesporsel = foresporsler.first()
            val oldestForesporsel = foresporsler.last()
            assertEquals(OTHER_VIRKSOMHETSNUMMER.value, newestForesporsel.virksomhetsnummer)
            assertEquals(VIRKSOMHETSNUMMER.value, oldestForesporsel.virksomhetsnummer)
        }
    }

    @Test
    fun `Returns OK with empty list when no foresporsler`() {
        testApplication {
            val client = setupApiAndClient()

            val response =
                client.get("$URL_OPPFOLGINGSPLAN/foresporsler") {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                }
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = response.body<List<ForesporselResponseDTO>>()
            assertEquals(emptyList(), responseBody)
        }
    }

    @Test
    fun `Returns status Unauthorized if no token is supplied`() {
        testApplication {
            val client = setupApiAndClient()
            val response = client.post("$URL_OPPFOLGINGSPLAN/foresporsler")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    @Test
    fun `Returns status Forbidden if denied access to person`() {
        testApplication {
            val client = setupApiAndClient()
            val foresporselRequestDTO = createForesporselRequestDTO(ARBEIDSTAKER_PERSONIDENT_VEILEDER_NO_ACCESS)
            val response =
                client.post("$URL_OPPFOLGINGSPLAN/foresporsler") {
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
            virksomhetsnummer = VIRKSOMHETSNUMMER.value,
            narmestelederPersonident = NARMESTELEDER_FNR.value,
            document = generateDocumentComponent(),
        )
}
