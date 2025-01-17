package no.nav.syfo

import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.api.apiModule
import no.nav.syfo.application.ForesporselService
import no.nav.syfo.infrastructure.clients.azuread.AzureAdClient
import no.nav.syfo.infrastructure.clients.dokarkiv.DokarkivClient
import no.nav.syfo.infrastructure.clients.ereg.EregClient
import no.nav.syfo.infrastructure.clients.pdfgen.PdfGenClient
import no.nav.syfo.infrastructure.clients.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.infrastructure.clients.wellknown.getWellKnown
import no.nav.syfo.infrastructure.cronjob.launchCronjobs
import no.nav.syfo.infrastructure.database.applicationDatabase
import no.nav.syfo.infrastructure.database.databaseModule
import no.nav.syfo.infrastructure.database.repository.ForesporselRepository
import no.nav.syfo.infrastructure.journalforing.JournalforingService
import no.nav.syfo.infrastructure.kafka.VarselProducer
import no.nav.syfo.infrastructure.kafka.esyfovarsel.EsyfovarselHendelse
import no.nav.syfo.infrastructure.kafka.esyfovarsel.EsyfovarselHendelseProducer
import no.nav.syfo.infrastructure.kafka.esyfovarsel.EsyfovarselHendelseSerializer
import no.nav.syfo.infrastructure.kafka.kafkaAivenProducerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

fun main() {
    val applicationState = ApplicationState()
    val environment = Environment()
    val logger = LoggerFactory.getLogger("ktor.application")

    val wellKnownInternalAzureAD =
        getWellKnown(
            wellKnownUrl = environment.azure.appWellKnownUrl,
        )
    val azureAdClient =
        AzureAdClient(
            azureEnvironment = environment.azure,
        )
    val veilederTilgangskontrollClient =
        VeilederTilgangskontrollClient(
            azureAdClient = azureAdClient,
            clientEnvironment = environment.clients.istilgangskontroll,
        )
    val kafkaProducer =
        KafkaProducer<String, EsyfovarselHendelse>(
            kafkaAivenProducerConfig<EsyfovarselHendelseSerializer>(
                kafkaEnvironment = environment.kafka
            ),
        )
    val narmesteLederVarselProducer =
        EsyfovarselHendelseProducer(
            producer = kafkaProducer,
        )
    val varselProducer =
        VarselProducer(
            narmesteLederVarselProducer = narmesteLederVarselProducer,
        )
    val dokarkivClient =
        DokarkivClient(
            azureAdClient = azureAdClient,
            dokarkivEnvironment = environment.clients.dokarkiv,
        )
    val pdfClient =
        PdfGenClient(
            pdfGenBaseUrl = environment.clients.ispdfgen.baseUrl,
        )
    val eregClient =
        EregClient(
            baseUrl = environment.clients.ereg.baseUrl,
        )
    val journalforingService =
        JournalforingService(
            dokarkivClient = dokarkivClient,
            eregClient = eregClient,
            pdfClient = pdfClient,
            isJournalforingRetryEnabled = environment.isJournalforingRetryEnabled,
        )
    val applicationEngineEnvironment =
        applicationEnvironment {
            log = logger
            config = HoconApplicationConfig(ConfigFactory.load())
        }
    val server =
        embeddedServer(
            factory = Netty,
            environment = applicationEngineEnvironment,
            configure = {
                connector {
                    port = 8080
                }
                connectionGroupSize = 8
                workerGroupSize = 8
                callGroupSize = 16
            },
            module = {
                databaseModule(
                    databaseEnvironment = environment.database,
                )

                val foresporselRepository = ForesporselRepository(applicationDatabase)
                val foresporselService =
                    ForesporselService(
                        varselProducer = varselProducer,
                        repository = foresporselRepository,
                        journalforingService = journalforingService,
                    )

                apiModule(
                    applicationState = applicationState,
                    environment = environment,
                    wellKnownInternalAzureAD = wellKnownInternalAzureAD,
                    database = applicationDatabase,
                    veilederTilgangskontrollClient = veilederTilgangskontrollClient,
                    foresporselService = foresporselService,
                )
                monitor.subscribe(ApplicationStarted) {
                    applicationState.ready = true
                    logger.info("Application is ready, running Java VM ${Runtime.version()}")
                    launchCronjobs(
                        applicationState = applicationState,
                        environment = environment,
                        foresporselService = foresporselService,
                    )
                }
            },
        )

    Runtime.getRuntime().addShutdownHook(
        Thread { server.stop(10, 10, TimeUnit.SECONDS) },
    )

    server.start(wait = true)
}
