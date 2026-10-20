package com.example.androidkeyboard.runtime

import java.io.IOException
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class R2ShellDumpCaptureTest {
    @get:Rule val temporary = TemporaryFolder()

    /** Same Runtime.exec argv semantics as Android 13 UiAutomationConnection. */
    private fun execute(command: String, input: String?): String {
        val process = Runtime.getRuntime().exec(command)
        process.outputStream.bufferedWriter().use { writer -> input?.let(writer::write) }
        assertTrue("synthetic shell command must terminate", process.waitFor(5, TimeUnit.SECONDS))
        return process.inputStream.bufferedReader().use { it.readText() }
    }

    @Test fun realShellRedirectionPersistsNonemptyRawEvidence() {
        val destination = temporary.root.resolve("first-input-method.txt")
        captureR2ShellDump("printf R2_SYNTHETIC_DUMP", destination.path, ::execute)
        assertTrue("raw file must actually exist, not only a completion marker", destination.isFile)
        assertEquals("R2_SYNTHETIC_DUMP", destination.readText())
    }

    @Test fun quotedDestinationRemainsInsideOwnedTemporaryDirectory() {
        val destination = temporary.newFolder("space and 'quote").resolve("first-window.txt")
        captureR2ShellDump("printf R2_SYNTHETIC_WINDOW", destination.path, ::execute)
        assertTrue(destination.isFile)
        assertEquals("R2_SYNTHETIC_WINDOW", destination.readText())
    }

    @Test fun failedDumpCommandMustNotReportSuccessfulPersistence() {
        val destination = temporary.root.resolve("failed.txt")
        assertThrows(IllegalStateException::class.java) {
            captureR2ShellDump("false", destination.path, ::execute)
        }
    }

    @Test fun emptyDumpMustNotReportSuccessfulPersistence() {
        val destination = temporary.root.resolve("empty.txt")
        assertThrows(IllegalStateException::class.java) {
            captureR2ShellDump("true", destination.path, ::execute)
        }
    }

    @Test fun transportFailureIsPreservedForOriginalAssertionBoundary() {
        val failure = IOException("synthetic transport failure")
        assertSame(failure, assertThrows(IOException::class.java) {
            captureR2ShellDump("printf R2", temporary.root.resolve("unwritten.txt").path) { _, _ -> throw failure }
        })
    }
}
