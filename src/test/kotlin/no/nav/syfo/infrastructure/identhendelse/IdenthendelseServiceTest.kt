package no.nav.syfo.infrastructure.identhendelse

import no.nav.syfo.ExternalMockEnvironment
import no.nav.syfo.UserConstants
import no.nav.syfo.generator.generateForesporsel
import no.nav.syfo.generator.generateIdenthendelse
import no.nav.syfo.infrastructure.database.dropData
import no.nav.syfo.infrastructure.database.repository.ForesporselRepository
import no.nav.syfo.infrastructure.kafka.identhendelse.IdenthendelseService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val aktivIdent = UserConstants.ARBEIDSTAKER_PERSONIDENT
private val inaktivIdent = UserConstants.ARBEIDSTAKER_PERSONIDENT_2
private val annenInaktivIdent = UserConstants.ARBEIDSTAKER_PERSONIDENT_VEILEDER_NO_ACCESS

class IdenthendelseServiceTest {
    private val externalMockEnvironment = ExternalMockEnvironment.instance
    private val database = externalMockEnvironment.database
    private val foresporselRepository = ForesporselRepository(database = database)
    private val identhendelseService =
        IdenthendelseService(
            repository = foresporselRepository,
        )

    @BeforeEach
    fun before() {
        database.dropData()
    }

    private val foresporselMedInaktivIdent = generateForesporsel(arbeidstakerPersonident = inaktivIdent)
    private val foresporselMedAnnenInaktivIdent = generateForesporsel(arbeidstakerPersonident = annenInaktivIdent)

    @Test
    fun `Flytter foresporsel fra inaktiv ident til ny ident nar person far ny ident`() {
        foresporselRepository.createForesporsel(foresporsel = foresporselMedInaktivIdent)

        val identhendelse =
            generateIdenthendelse(
                aktivIdent = aktivIdent,
                inaktiveIdenter = listOf(inaktivIdent)
            )
        identhendelseService.handle(identhendelse)

        assertTrue(foresporselRepository.getForesporsler(personident = inaktivIdent).isEmpty())
        assertTrue(foresporselRepository.getForesporsler(personident = aktivIdent).isNotEmpty())
    }

    @Test
    fun `Flytter foresporseler fra inaktiv ident til ny ident nar person far ny ident`() {
        foresporselRepository.createForesporsel(foresporsel = foresporselMedInaktivIdent)
        foresporselRepository.createForesporsel(foresporsel = foresporselMedAnnenInaktivIdent)

        val identhendelse =
            generateIdenthendelse(
                aktivIdent = aktivIdent,
                inaktiveIdenter = listOf(inaktivIdent, annenInaktivIdent)
            )
        identhendelseService.handle(identhendelse)

        assertTrue(foresporselRepository.getForesporsler(personident = inaktivIdent).isEmpty())
        assertTrue(foresporselRepository.getForesporsler(personident = annenInaktivIdent).isEmpty())
        assertEquals(2, foresporselRepository.getForesporsler(personident = aktivIdent).size)
    }

    @Test
    fun `Oppdaterer ingenting nar person far ny ident og uten foresporsel pa inaktiv ident`() {
        val identhendelse =
            generateIdenthendelse(
                aktivIdent = aktivIdent,
                inaktiveIdenter = listOf(inaktivIdent)
            )
        identhendelseService.handle(identhendelse)

        assertTrue(foresporselRepository.getForesporsler(personident = inaktivIdent).isEmpty())
        assertTrue(foresporselRepository.getForesporsler(personident = aktivIdent).isEmpty())
    }

    @Test
    fun `Oppdaterer ingenting nar person far ny ident uten inaktive identer`() {
        foresporselRepository.createForesporsel(foresporsel = foresporselMedInaktivIdent)

        val identhendelse =
            generateIdenthendelse(
                aktivIdent = aktivIdent,
                inaktiveIdenter = emptyList()
            )
        identhendelseService.handle(identhendelse)

        assertTrue(foresporselRepository.getForesporsler(personident = inaktivIdent).isNotEmpty())
        assertTrue(foresporselRepository.getForesporsler(personident = aktivIdent).isEmpty())
    }

    @Test
    fun `Oppdaterer ingenting nar person mangler aktiv ident`() {
        foresporselRepository.createForesporsel(foresporsel = foresporselMedInaktivIdent)

        val identhendelse =
            generateIdenthendelse(
                aktivIdent = null,
                inaktiveIdenter = listOf(inaktivIdent)
            )
        identhendelseService.handle(identhendelse)

        assertTrue(foresporselRepository.getForesporsler(personident = inaktivIdent).isNotEmpty())
        assertTrue(foresporselRepository.getForesporsler(personident = aktivIdent).isEmpty())
    }
}
