package no.nav.syfo

import no.nav.syfo.common.token.azuread.AzureAdClientConfig
import no.nav.syfo.common.util.ClientConfig
import no.nav.syfo.common.util.OpenClientConfig
import no.nav.syfo.infrastructure.clients.ClientsConfig
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
            AzureAdClientConfig(
                appClientId = "isoppfolgingsplan-client-id",
                appClientSecret = "isoppfolgingsplan-secret",
                appWellKnownUrl = "wellknown",
                openidConfigTokenEndpoint = "azureOpenIdTokenEndpoint",
            ),
        clients =
            ClientsConfig(
                istilgangskontroll =
                    ClientConfig(
                        baseUrl = "isTilgangskontrollUrl",
                        clientId = "dev-gcp.teamsykefravr.istilgangskontroll",
                    ),
                dokarkiv =
                    ClientConfig(
                        baseUrl = "dokarkiv",
                        clientId = "dev-gcp.teamsykefravr.dokarkiv",
                    ),
                ereg =
                    OpenClientConfig(
                        baseUrl = "ereg",
                    ),
                ispdfgen =
                    OpenClientConfig(
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
