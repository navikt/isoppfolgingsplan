package no.nav.syfo.infrastructure.database.repository

import no.nav.syfo.ExternalMockEnvironment
import no.nav.syfo.generator.generateForsporsel
import no.nav.syfo.infrastructure.database.dropData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.temporal.ChronoUnit
import java.util.*

object ForesporselRepositoryTest {
    private val externalMockEnvironment = ExternalMockEnvironment.instance
    private val database = externalMockEnvironment.database
    private val foresporselRepository = ForesporselRepository(database)

    @BeforeEach
    fun setup() {
        database.dropData()
    }

    @Test
    fun `creates a new Foresporsel`() {
        val foresporsel = generateForsporsel()

        val createdForesporsel = foresporselRepository.createForesporsel(foresporsel)

        assertEquals(createdForesporsel.uuid, foresporsel.uuid)
        assertEquals(createdForesporsel.arbeidstakerPersonident, foresporsel.arbeidstakerPersonident)
        assertEquals(createdForesporsel.virksomhetsnummer, foresporsel.virksomhetsnummer)
        assertEquals(createdForesporsel.narmestelederPersonident, foresporsel.narmestelederPersonident)
        assertEquals(
            createdForesporsel.createdAt.truncatedTo(ChronoUnit.MILLIS),
            foresporsel.createdAt.truncatedTo(ChronoUnit.MILLIS)
        )

        createdForesporsel.uuid shouldBeEqualTo foresporsel.uuid
    }

    @Test
    fun `gets Foresporsel`() {
        val foresporsel = generateForsporsel()
        val createdForesporsel = foresporselRepository.createForesporsel(foresporsel)

        val fetchedForesporsel = foresporselRepository.getForesporsel(foresporsel.uuid)

        assertEquals(fetchedForesporsel, createdForesporsel)
    }

    @Test
    fun `gets null when no Foresporsel`() {
        val fetchedForesporsel = foresporselRepository.getForesporsel(UUID.randomUUID())

        assertEquals(fetchedForesporsel, null)
    }
}

infix fun <T> T.shouldBeEqualTo(other: T) = assertEquals(this, other)
