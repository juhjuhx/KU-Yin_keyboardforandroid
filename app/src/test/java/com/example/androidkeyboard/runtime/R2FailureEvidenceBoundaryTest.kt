package com.example.androidkeyboard.runtime

import java.io.IOException
import org.junit.Assert.*
import org.junit.Test

class R2FailureEvidenceBoundaryTest {
    @Test fun ioFailureCannotReplaceOriginalVisibilityAssertion() {
        val transport = IOException("synthetic transport failure")
        val primary = AssertionError("KU-Yin input window should become visible")
        val actual = assertThrows(AssertionError::class.java) {
            withR2FailureEvidence({ throw transport }) { throw primary }
        }
        assertSame(primary, actual)
        assertArrayEquals(arrayOf(transport), actual.suppressed)
    }

    @Test fun disconnectedAutomationCannotReplaceRecreateAssertion() {
        val transport = IllegalStateException("synthetic disconnected automation")
        val actual = assertThrows(AssertionError::class.java) {
            withR2FailureEvidence({ throw transport }) {
                assertTrue("KU-Yin should survive editor recreation", false)
            }
        }
        assertEquals("KU-Yin should survive editor recreation", actual.message)
        assertSame(transport, actual.suppressed.single())
    }

    @Test fun passingAssertionDoesNotHideDiagnosticFailure() {
        val transport = IOException("synthetic capture failed")
        assertSame(transport, assertThrows(IOException::class.java) {
            withR2FailureEvidence({ throw transport }) { assertTrue(true) }
        })
    }

    @Test fun successfulDiagnosticsRunBeforeUnchangedAssertion() {
        val sequence = mutableListOf<String>()
        withR2FailureEvidence({ sequence += "capture" }) { sequence += "assert"; assertTrue(true) }
        assertEquals(listOf("capture", "assert"), sequence)
    }
}
