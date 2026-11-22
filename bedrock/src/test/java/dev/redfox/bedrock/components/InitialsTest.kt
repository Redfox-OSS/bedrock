package dev.redfox.bedrock.components

import org.junit.Assert.assertEquals
import org.junit.Test

class InitialsTest {
    @Test fun firstAndLastWords() = assertEquals("MF", initials("Maya Fernandes"))
    @Test fun middleNamesAreSkipped() = assertEquals("HN", initials("Halima Ama Ncube"))
    @Test fun singleWord() = assertEquals("L", initials("leo"))
    @Test fun extraSpaces() = assertEquals("AR", initials("  Arjun   Rao "))
    @Test fun empty() = assertEquals("?", initials("   "))
}
