package no.nav.syfo.infrastructure.database

import no.nav.syfo.isLocal

lateinit var applicationDatabase: DatabaseInterface

fun databaseModule(
    databaseEnvironment: DatabaseEnvironment
) = if (isLocal()) {
    applicationDatabase = Database(
        DatabaseConfig(
            jdbcUrl = "jdbc:postgresql://localhost:5432/isoppfolgingsplan_dev",
            password = "password",
            username = "username"
        )
    )
} else {
    applicationDatabase = Database(
        DatabaseConfig(
            jdbcUrl = databaseEnvironment.url,
            username = databaseEnvironment.username,
            password = databaseEnvironment.password
        )
    )
}
