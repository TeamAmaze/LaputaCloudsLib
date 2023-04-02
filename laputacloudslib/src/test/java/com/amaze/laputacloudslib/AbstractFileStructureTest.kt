package com.amaze.laputacloudslib

import org.junit.Test

import org.junit.Assert.*

class AbstractFileStructureTest {
    @Test
    fun test_AbstractFileStructure_sanitizeRawPath() {
        assertEquals("/",
            AbstractCloudPath.sanitizeRawPath(
                "a/b/../../"
            )
        )
    }
}
