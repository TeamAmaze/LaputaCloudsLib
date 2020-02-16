package com.amaze.laputacloudslib

import org.junit.Test

import org.junit.Assert.*

class AbstractFileStructureTest {
    @Test
    fun test_AbstractFileStructure_removeScheme() {
        assertEquals("/fs/..",
            AbstractFileStructureDriver.removeScheme(
                "a:/fs/..",
                "a:"
            )
        )
    }

    @Test
    fun test_AbstractFileStructure_sanitizeRawPath() {
        assertEquals("/",
            AbstractFileStructureDriver.sanitizeRawPath(
                "a/b/../../"
            )
        )
    }
}
