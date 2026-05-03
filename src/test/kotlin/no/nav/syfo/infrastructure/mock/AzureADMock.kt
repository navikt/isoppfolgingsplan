package no.nav.syfo.infrastructure.mock

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*

private data class AzureAdTokenResponse(
    val access_token: String,
    val expires_in: Long,
    val token_type: String,
)

fun MockRequestHandleScope.azureAdMockResponse(): HttpResponseData =
    respond(
        AzureAdTokenResponse(
            access_token = "token",
            expires_in = 3600,
            token_type = "type",
        ),
    )
