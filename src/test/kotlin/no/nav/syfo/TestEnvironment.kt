package no.nav.syfo

import no.nav.syfo.infrastructure.clients.ClientEnvironment
import no.nav.syfo.infrastructure.clients.ClientsEnvironment
import no.nav.syfo.infrastructure.clients.OpenClientEnvironment
import no.nav.syfo.infrastructure.clients.azuread.AzureEnvironment
import no.nav.syfo.infrastructure.database.DatabaseEnvironment
import no.nav.syfo.infrastructure.kafka.KafkaEnvironment

fun testEnvironment() =
    Environment(
        database =
            DatabaseEnvironment(
                host = "localhost",
                port = "5432",
                name = "isoppfolgingsplan_dev",
                username = "username",
                password = "password",
                url = "jdbc:postgresql://localhost:5432/isoppfolgingsplan_dev",
            ),
        kafka =
            KafkaEnvironment(
                aivenBootstrapServers = "kafkaBootstrapServers",
                aivenCredstorePassword = "credstorepassord",
                aivenKeystoreLocation = "keystore",
                aivenSecurityProtocol = "SSL",
                aivenTruststoreLocation = "truststore",
                aivenSchemaRegistryUrl = "http://kafka-schema-registry.tpa.svc.nais.local:8081",
                aivenRegistryUser = "registryuser",
                aivenRegistryPassword = "registrypassword",
            ),
        azure =
            AzureEnvironment(
                appClientId = "isoppfolgingsplan-client-id",
                appClientSecret = "isoppfolgingsplan-secret",
                appWellKnownUrl = "wellknown",
                openidConfigTokenEndpoint = "azureOpenIdTokenEndpoint",
            ),
        clients =
            ClientsEnvironment(
                istilgangskontroll =
                    ClientEnvironment(
                        baseUrl = "isTilgangskontrollUrl",
                        clientId = "dev-gcp.teamsykefravr.istilgangskontroll",
                    ),
                dokarkiv =
                    ClientEnvironment(
                        baseUrl = "dokarkiv",
                        clientId = "dev-gcp.teamsykefravr.dokarkiv",
                    ),
                ereg =
                    OpenClientEnvironment(
                        baseUrl = "ereg",
                    ),
                ispdfgen =
                    OpenClientEnvironment(
                        baseUrl = "ispdfgen",
                    ),
            ),
        electorPath = "electorPath",
        isJournalforingRetryEnabled = true,
    )

fun testAppState() =
    ApplicationState(
        alive = true,
        ready = true,
    )
