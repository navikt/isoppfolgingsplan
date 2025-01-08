package no.nav.syfo.infrastructure.database

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import no.nav.syfo.infrastructure.database.repository.PForesporsel
import no.nav.syfo.infrastructure.database.repository.toPForesporsel
import org.flywaydb.core.Flyway
import java.sql.Connection
import java.util.*

class TestDatabase : DatabaseInterface {
    private val pg: EmbeddedPostgres =
        try {
            EmbeddedPostgres.start()
        } catch (e: Exception) {
            EmbeddedPostgres.builder().start()
        }

    override val connection: Connection
        get() = pg.postgresDatabase.connection.apply { autoCommit = false }

    init {

        Flyway.configure().run {
            dataSource(pg.postgresDatabase).validateMigrationNaming(true).load().migrate()
        }
    }

    fun stop() {
        pg.close()
    }
}

fun TestDatabase.dropData() {
    val queryList =
        listOf(
            """
            DELETE FROM FORESPORSEL
            """.trimIndent(),
        )
    this.connection.use { connection ->
        queryList.forEach { query ->
            connection.prepareStatement(query).execute()
        }
        connection.commit()
    }
}

fun TestDatabase.getForesporsel(uuid: UUID): PForesporsel {
    val query =
        """
        SELECT *
        FROM foresporsel
        WHERE uuid = ?
        """
    return this.connection.use { connection ->
        connection.prepareStatement(query).use {
            it.setString(1, uuid.toString())
            it.executeQuery().toList { toPForesporsel() }.single()
        }
    }
}

class TestDatabaseNotResponding : DatabaseInterface {
    override val connection: Connection
        get() = throw Exception("Not working")
}
