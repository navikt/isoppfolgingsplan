package no.nav.syfo.application

import no.nav.syfo.domain.JournalpostId
import no.nav.syfo.domain.Foresporsel

interface IJournalforingService {
    suspend fun journalfor(
        foresporsel: Foresporsel,
        pdf: ByteArray,
    ): Result<JournalpostId>
}
