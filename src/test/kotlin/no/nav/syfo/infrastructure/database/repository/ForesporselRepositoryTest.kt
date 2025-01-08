package no.nav.syfo.infrastructure.database.repository

import no.nav.syfo.ExternalMockEnvironment
import no.nav.syfo.UserConstants
import no.nav.syfo.generator.generateForsporsel
import no.nav.syfo.infrastructure.database.dropData
import no.nav.syfo.infrastructure.database.getForesporsel
import no.nav.syfo.shouldBeEqualTo
import no.nav.syfo.shouldNotBeEqualTo
import org.junit.jupiter.api.*
import java.sql.SQLException
import java.time.temporal.ChronoUnit
import java.util.*

class ForesporselRepositoryTest {
    private val externalMockEnvironment = ExternalMockEnvironment.instance
    private val database = externalMockEnvironment.database
    private val foresporselRepository = externalMockEnvironment.foresporselRepository

    private val foresporsel = generateForsporsel()

    @BeforeEach
    fun setup() {
        database.dropData()
    }

    @Nested
    @DisplayName("Create new Foresporsel")
    inner class CreateForesporselTests {
        @Test
        fun `creates a new Foresporsel`() {
            val createdForesporsel = foresporselRepository.createForesporsel(foresporsel)

            createdForesporsel.uuid shouldBeEqualTo foresporsel.uuid
            createdForesporsel.arbeidstakerPersonident shouldBeEqualTo foresporsel.arbeidstakerPersonident
            createdForesporsel.virksomhetsnummer shouldBeEqualTo foresporsel.virksomhetsnummer
            createdForesporsel.narmestelederPersonident shouldBeEqualTo foresporsel.narmestelederPersonident
            createdForesporsel.createdAt.truncatedTo(ChronoUnit.MILLIS) shouldBeEqualTo
                foresporsel.createdAt.truncatedTo(ChronoUnit.MILLIS)
        }
    }

    @Nested
    @DisplayName("Get Foresporsler for personident")
    inner class GetForesporslerTests {
        @Test
        fun `gets Foresporsler`() {
            val createdForesporsel = foresporselRepository.createForesporsel(foresporsel)

            val fetchedForesporsel = foresporselRepository.getForesporsler(foresporsel.arbeidstakerPersonident)

            fetchedForesporsel.first() shouldBeEqualTo createdForesporsel
        }

        @Test
        fun `gets Foresporsler only for given personident`() {
            val otherForesporsel = generateForsporsel(UserConstants.ARBEIDSTAKER_PERSONIDENT_2)
            foresporselRepository.createForesporsel(foresporsel)
            foresporselRepository.createForesporsel(otherForesporsel)

            val fetchedForesporsel = foresporselRepository.getForesporsler(foresporsel.arbeidstakerPersonident)

            fetchedForesporsel.size shouldBeEqualTo 1
            fetchedForesporsel.first().arbeidstakerPersonident shouldBeEqualTo foresporsel.arbeidstakerPersonident
            fetchedForesporsel.first().uuid shouldBeEqualTo foresporsel.uuid
        }

        @Test
        fun `gets several Foresporsler for given personident`() {
            val otherForesporsel = generateForsporsel()
            foresporselRepository.createForesporsel(foresporsel)
            foresporselRepository.createForesporsel(otherForesporsel)

            val fetchedForesporsel = foresporselRepository.getForesporsler(foresporsel.arbeidstakerPersonident)

            fetchedForesporsel.size shouldBeEqualTo 2
        }

        @Test
        fun `gets null when no Foresporsel`() {
            val fetchedForesporsel =
                foresporselRepository.getForesporsler(
                    personident = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                )

            fetchedForesporsel.size shouldBeEqualTo 0
        }
    }

    @Nested
    @DisplayName("Update publishedAt")
    inner class PublishedAtTests {
        @Test
        fun `updates publishedAt for Foresporsel`() {
            val createdForesporsel = foresporselRepository.createForesporsel(foresporsel)

            foresporselRepository.setPublishedAt(createdForesporsel.uuid)

            val pForesporsel = database.getForesporsel(createdForesporsel.uuid)

            pForesporsel.publishedAt shouldNotBeEqualTo null
        }

        @Test
        fun `failes when no Foresporsel for uuid`() {
            assertThrows<SQLException> {
                foresporselRepository.setPublishedAt(UUID.randomUUID())
            }
        }
    }
}
