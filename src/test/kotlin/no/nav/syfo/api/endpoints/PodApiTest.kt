package no.nav.syfo.api.endpoints

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import no.nav.syfo.ApplicationState
import no.nav.syfo.infrastructure.database.DatabaseInterface
import no.nav.syfo.infrastructure.database.TestDatabase
import no.nav.syfo.infrastructure.database.TestDatabaseNotResponding
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

object PodApiTest {
    private val database = TestDatabase()
    private val databaseNotResponding = TestDatabaseNotResponding()

    private fun ApplicationTestBuilder.setupPodApi(
        database: DatabaseInterface,
        applicationState: ApplicationState,
    ) {
        application {
            routing {
                podEndpoints(
                    applicationState = applicationState,
                    database = database,
                )
            }
        }
    }

    @Test
    fun `Returns ok on is_alive when alive and ready`() =
        testApplication {
            setupPodApi(
                database = database,
                applicationState = ApplicationState(alive = true, ready = true),
            )

            val response = client.get("/internal/is_alive")
            assertTrue(response.status.isSuccess())
            assertNotNull(response.bodyAsText())
        }

    @Test
    fun `Returns ok on is_ready when alive and ready`() =
        testApplication {
            setupPodApi(
                database = database,
                applicationState = ApplicationState(alive = true, ready = true),
            )

            val response = client.get("/internal/is_ready")
            assertTrue(response.status.isSuccess())
            assertNotNull(response.bodyAsText())
        }

    @Test
    fun `Returns error on is_alive when not alive or ready`() =
        testApplication {
            setupPodApi(
                database = database,
                applicationState = ApplicationState(alive = false, ready = false),
            )

            val response = client.get("/internal/is_alive")
            assertEquals(HttpStatusCode.InternalServerError, response.status)
            assertNotNull(response.bodyAsText())
        }

    @Test
    fun `Returns error on is_ready when not alive or ready`() =
        testApplication {
            setupPodApi(
                database = database,
                applicationState = ApplicationState(alive = false, ready = false),
            )

            val response = client.get("/internal/is_ready")
            assertEquals(HttpStatusCode.InternalServerError, response.status)
            assertNotNull(response.bodyAsText())
        }

    @Test
    fun `Returns ok on is_alive when alive and ready and database not responding`() =
        testApplication {
            setupPodApi(
                database = databaseNotResponding,
                applicationState = ApplicationState(alive = true, ready = true),
            )

            val response = client.get("/internal/is_alive")
            assertTrue(response.status.isSuccess())
            assertNotNull(response.bodyAsText())
        }

    @Test
    fun `Returns error on is_ready when alive and ready and database not responding`() =
        testApplication {
            setupPodApi(
                database = databaseNotResponding,
                applicationState = ApplicationState(alive = true, ready = true),
            )

            val response = client.get("/internal/is_ready")
            assertEquals(HttpStatusCode.InternalServerError, response.status)
            assertNotNull(response.bodyAsText())
        }
}
