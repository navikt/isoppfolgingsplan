package no.nav.syfo.infrastructure.database.repository

import com.fasterxml.jackson.core.type.TypeReference
import no.nav.syfo.application.IForesporselRepository
import no.nav.syfo.domain.*
import no.nav.syfo.infrastructure.database.DatabaseInterface
import no.nav.syfo.infrastructure.database.toList
import no.nav.syfo.util.configuredJacksonMapper
import no.nav.syfo.util.nowUTC
import java.sql.ResultSet
import java.sql.SQLException
import java.time.OffsetDateTime
import java.util.*

private val mapper = configuredJacksonMapper()

class ForesporselRepository(val database: DatabaseInterface) : IForesporselRepository {
    override fun createForesporsel(foresporsel: Foresporsel): Foresporsel {
        return database.connection.use { connection ->
            val pForesporsel =
                connection.prepareStatement(CREATE_FORESPORSEL).use {
                    it.setString(1, foresporsel.uuid.toString())
                    it.setObject(2, foresporsel.createdAt)
                    it.setString(3, foresporsel.arbeidstakerPersonident.value)
                    it.setString(4, foresporsel.veilederident.value)
                    it.setString(5, foresporsel.narmestelederPersonident.value)
                    it.setString(6, foresporsel.virksomhetsnummer.value)
                    it.setObject(7, mapper.writeValueAsString(foresporsel.document))
                    it.executeQuery().toList { toPForesporsel() }.single()
                }
            connection.commit()
            pForesporsel.toForesporsel()
        }
    }

    override fun getForesporsler(personident: Personident): List<Foresporsel> {
        return database.connection.use { connection ->
            connection.prepareStatement(GET_FORESPORSEL).use {
                it.setString(1, personident.value)
                it.executeQuery()
                    .toList { toPForesporsel().toForesporsel() }
            }
        }
    }

    override fun setPublishedAt(foresporselUuid: UUID) {
        database.connection.use { connection ->
            connection.prepareStatement(SET_PUBLISHED_AT).use {
                it.setObject(1, nowUTC())
                it.setString(2, foresporselUuid.toString())
                val updated = it.executeUpdate()
                if (updated != 1) {
                    throw SQLException("Expected a single row to be updated, got update count $updated")
                }
            }
            connection.commit()
        }
    }

    override fun getForesporslerForJournalforing(): List<Foresporsel> {
        return database.connection.use { connection ->
            connection.prepareStatement(GET_FORESPORSEL_JOURNALFORING).use {
                it.executeQuery()
                    .toList { toPForesporsel().toForesporsel() }
            }
        }
    }

    override fun setJournalpostId(journalfortForesporsel: Foresporsel) {
        return database.connection.use { connection ->
            connection.prepareStatement(SET_FORESPORSEL_JOURNALFORING).use {
                it.setString(1, journalfortForesporsel.journalpostId?.value)
                it.setString(2, journalfortForesporsel.uuid.toString())
                val updated = it.executeUpdate()
                if (updated != 1) {
                    throw SQLException("Expected a single row to be updated, got update count $updated")
                }
            }
            connection.commit()
        }
    }

    override fun getUnpublishedForesporsler(): List<Foresporsel> =
        database.connection.use { connection ->
            connection.prepareStatement(GET_UNPUBLISHED_FORESPORSEL).use {
                it.executeQuery()
                    .toList { toPForesporsel().toForesporsel() }
            }
        }

    override fun updateArbeidstakerPersonident(
        nyPersonident: Personident,
        foresporsler: List<Foresporsel>
    ) = database.connection.use { connection ->
        connection.prepareStatement(UPDATE_FORESPORSEL_ARBEIDSTAKER_PERSONIDENT).use {
            foresporsler.forEach { foresporsel ->
                it.setString(1, nyPersonident.value)
                it.setString(2, foresporsel.uuid.toString())
                val updated = it.executeUpdate()
                if (updated != 1) {
                    throw SQLException("Expected a single row to be updated, got update count $updated")
                }
            }
        }
        connection.commit()
    }

    companion object {
        private const val CREATE_FORESPORSEL =
            """
                INSERT INTO foresporsel (
                    id,
                    uuid,
                    created_at,
                    arbeidstaker_personident,
                    veilederident,
                    narmesteleder_personident,
                    virksomhetsnummer,
                    document
                ) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?::jsonb)
                RETURNING *
            """

        private const val GET_FORESPORSEL =
            """
                SELECT *
                FROM foresporsel
                WHERE arbeidstaker_personident = ?
                ORDER BY created_at DESC
            """

        private const val SET_PUBLISHED_AT =
            """
                UPDATE foresporsel
                SET published_at = ?
                WHERE uuid = ?
            """

        private const val GET_FORESPORSEL_JOURNALFORING =
            """
                SELECT *
                FROM foresporsel
                WHERE journalpost_id IS NULL
                ORDER BY ID ASC
            """

        private const val SET_FORESPORSEL_JOURNALFORING =
            """
                UPDATE foresporsel SET journalpost_id=? WHERE uuid=?
            """

        private const val GET_UNPUBLISHED_FORESPORSEL =
            """
                SELECT *
                FROM foresporsel
                WHERE published_at IS NULL
                ORDER BY created_at ASC
            """

        private const val UPDATE_FORESPORSEL_ARBEIDSTAKER_PERSONIDENT =
            """
            UPDATE foresporsel
            SET arbeidstaker_personident = ?
            WHERE uuid = ?
            """
    }
}

internal fun ResultSet.toPForesporsel(): PForesporsel =
    PForesporsel(
        id = getInt("id"),
        uuid = UUID.fromString(getString("uuid")),
        createdAt = getObject("created_at", OffsetDateTime::class.java),
        arbeidstakerPersonident = Personident(getString("arbeidstaker_personident")),
        veilederident = Veilederident(getString("veilederident")),
        narmestelederPersonident = Personident(getString("narmesteleder_personident")),
        virksomhetsnummer = Virksomhetsnummer(getString("virksomhetsnummer")),
        publishedAt = getObject("published_at", OffsetDateTime::class.java),
        journalpostId = getString("journalpost_id")?.let { JournalpostId(it) },
        document =
            mapper.readValue(
                getString("document"),
                object : TypeReference<List<DocumentComponent>>() {}
            ),
    )
