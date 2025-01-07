package no.nav.syfo

import org.junit.jupiter.api.Assertions.assertEquals

infix fun <T> T.shouldBeEqualTo(other: T) = assertEquals(this, other)
