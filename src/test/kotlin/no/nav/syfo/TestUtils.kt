package no.nav.syfo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals

infix fun <T> T.shouldBeEqualTo(other: T) = assertEquals(this, other)

infix fun <T> T.shouldNotBeEqualTo(other: T) = assertNotEquals(this, other)
