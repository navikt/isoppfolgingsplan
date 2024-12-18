package no.nav.syfo.domain

@JvmInline
value class Virksomhetsnummer(val value: String) {
    init {
        if (!Regex("^\\d{9}\$").matches(value)) {
            throw IllegalArgumentException("$value is not a valid Virksomhetsnummer")
        }
    }
}
