package no.nav.syfo.infrastructure.clients.ereg

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.domain.Virksomhetsnummer
import no.nav.syfo.infrastructure.clients.httpClientDefault
import org.slf4j.LoggerFactory

class EregClient(
    baseUrl: String,
    private val httpClient: HttpClient = httpClientDefault(),
) {
    private val eregOrganisasjonUrl: String = "$baseUrl/$EREG_PATH"

    suspend fun organisasjonVirksomhetsnavn(virksomhetsnummer: Virksomhetsnummer): EregVirksomhetsnavn? {
        return try {
            val url = "$eregOrganisasjonUrl/${virksomhetsnummer.value}"
            val response =
                httpClient.get(url) {
                    accept(ContentType.Application.Json)
                }.body<EregOrganisasjonResponse>()
            COUNT_CALL_EREG_ORGANISASJON_SUCCESS.increment()
            response.toEregVirksomhetsnavn()
        } catch (e: ResponseException) {
            if (e.isOrganisasjonNotFound(virksomhetsnummer)) {
                log.warn("No Organisasjon was found in Ereg: returning empty Virksomhetsnavn, message=${e.message}")
                COUNT_CALL_EREG_ORGANISASJON_NOT_FOUND.increment()
                EregVirksomhetsnavn(
                    virksomhetsnavn = "Ukjent",
                )
            } else {
                log.error(
                    "Error while requesting Response from Ereg {}, {}",
                    StructuredArguments.keyValue("statusCode", e.response.status.value.toString()),
                    StructuredArguments.keyValue("message", e.message),
                )
                COUNT_CALL_EREG_ORGANISASJON_FAIL.increment()
                throw RuntimeException(e)
            }
        }
    }

    private fun ResponseException.isOrganisasjonNotFound(virksomhetsnummer: Virksomhetsnummer): Boolean {
        val is404 = this.response.status == HttpStatusCode.NotFound
        val messageNoVirksomhetsnavn =
            "Ingen organisasjon med organisasjonsnummer ${virksomhetsnummer.value} ble funnet"
        val isMessageNoVirksomhetsnavn = this.message?.contains(messageNoVirksomhetsnavn) ?: false
        return is404 && isMessageNoVirksomhetsnavn
    }

    companion object {
        const val EREG_PATH = "ereg/api/v1/organisasjon"
        private val log = LoggerFactory.getLogger(EregClient::class.java)
    }
}
