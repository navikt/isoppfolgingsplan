package no.nav.syfo.infrastructure.database.repository

import no.nav.syfo.ExternalMockEnvironment
import no.nav.syfo.UserConstants
import no.nav.syfo.generator.generateForsporsel
import no.nav.syfo.infrastructure.database.dropData
import no.nav.syfo.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.temporal.ChronoUnit
import java.util.*

class ForesporselRepositoryTest {
    private val externalMockEnvironment = ExternalMockEnvironment.instance
    private val database = externalMockEnvironment.database
    private val foresporselRepository = ForesporselRepository(database)

    @BeforeEach
    fun setup() {
        database.dropData()
    }

    @Nested
    @DisplayName("Create new Foresporsel")
    inner class CreateForesporselTests {
        @Test
        fun `creates a new Foresporsel`() {
            val foresporsel = generateForsporsel()

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
            val foresporsel = generateForsporsel()
            val createdForesporsel = foresporselRepository.createForesporsel(foresporsel)

            val fetchedForesporsel = foresporselRepository.getForesporsler(foresporsel.arbeidstakerPersonident)

            fetchedForesporsel.first() shouldBeEqualTo createdForesporsel
        }

        @Test
        fun `gets Foresporsler only for given personident`() {
            val foresporsel = generateForsporsel()
            val otherForesporsel =
                foresporsel.copy(
                    uuid = UUID.randomUUID(),
                    arbeidstakerPersonident = UserConstants.ARBEIDSTAKER_PERSONIDENT_2,
                )
            foresporselRepository.createForesporsel(foresporsel)
            foresporselRepository.createForesporsel(otherForesporsel)

            val fetchedForesporsel = foresporselRepository.getForesporsler(foresporsel.arbeidstakerPersonident)

            fetchedForesporsel.size shouldBeEqualTo 1
            fetchedForesporsel.first().arbeidstakerPersonident shouldBeEqualTo foresporsel.arbeidstakerPersonident
            fetchedForesporsel.first().uuid shouldBeEqualTo foresporsel.uuid
        }

        @Test
        fun `gets several Foresporsler for given personident`() {
            val foresporsel = generateForsporsel()
            val otherForesporsel =
                foresporsel.copy(
                    uuid = UUID.randomUUID(),
                )
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
}
