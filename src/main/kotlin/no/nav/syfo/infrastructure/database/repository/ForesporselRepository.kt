package no.nav.syfo.infrastructure.database.repository

import no.nav.syfo.application.IForesporselRepository
import no.nav.syfo.domain.Foresporsel
import no.nav.syfo.domain.Personident
import no.nav.syfo.domain.Veilederident
import no.nav.syfo.domain.Virksomhetsnummer
import no.nav.syfo.infrastructure.database.DatabaseInterface
import no.nav.syfo.infrastructure.database.toList
import no.nav.syfo.util.nowUTC
import java.sql.ResultSet
import java.sql.SQLException
import java.time.OffsetDateTime
import java.util.*

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
                    virksomhetsnummer
                ) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)
                RETURNING *
            """

        private const val GET_FORESPORSEL =
            """
                SELECT *
                FROM foresporsel
                WHERE arbeidstaker_personident = ?
            """

        private const val SET_PUBLISHED_AT =
            """
                UPDATE foresporsel
                SET published_at = ?
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
    )
