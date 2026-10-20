package com.example.androidkeyboard.runtime

/** TEST_ONLY. Preserve the original assertion when diagnostic transport also fails. */
internal fun withR2FailureEvidence(diagnostics: () -> Unit, assertion: () -> Unit) {
    val diagnosticError = try {
        diagnostics()
        null
    } catch (error: Exception) {
        error
    }
    try {
        assertion()
    } catch (primary: AssertionError) {
        diagnosticError?.let { primary.addSuppressed(it) }
        throw primary
    }
    diagnosticError?.let { throw it }
}
