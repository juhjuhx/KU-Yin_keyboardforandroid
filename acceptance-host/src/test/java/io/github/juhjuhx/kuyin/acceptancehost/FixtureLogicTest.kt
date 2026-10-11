package io.github.juhjuhx.kuyin.acceptancehost

import org.junit.Assert.*
import org.junit.Test

class FixtureLogicTest {
    @Test fun defaultsAreExplicit() {
        assertEquals(CaseConfig(), CaseConfig.fromExtras(emptyMap()))
        assertEquals(1, CaseConfig().inputType)
        assertTrue(CaseConfig().performEditorActionReturnValue)
    }
    @Test fun parsesAllPrimitiveExtras() {
        assertEquals(CaseConfig("D11", 1, 4, "確認", 73, false, "seed", 3),
            CaseConfig.fromExtras(mapOf("caseId" to "D11", "inputType" to 1,
                "imeOptions" to 4, "customActionLabel" to "確認", "customActionId" to 73,
                "performEditorActionReturnValue" to false, "initialText" to "seed", "minLines" to 3)))
    }
    @Test fun preservesTypeNull() {
        assertEquals(0, CaseConfig.fromExtras(mapOf("inputType" to 0)).inputType)
    }
    @Test fun emptyCustomLabelIsNotMissing() {
        assertEquals("", CaseConfig.fromExtras(mapOf("customActionLabel" to "")).customActionLabel)
    }
    @Test fun customIdZeroIsValid() {
        val config = CaseConfig.fromExtras(mapOf("customActionLabel" to "確認", "customActionId" to 0))
        assertEquals("確認", config.customActionLabel)
        assertEquals(0, config.customActionId)
    }
    @Test fun unknownExtrasDoNotAffectDefaults() {
        assertEquals(CaseConfig(), CaseConfig.fromExtras(mapOf("other" to 42)))
    }
    @Test fun nullLabelIsAllowed() {
        assertNull(CaseConfig.fromExtras(mapOf("customActionLabel" to null)).customActionLabel)
    }
    @Test fun wrongIntTypeIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { CaseConfig.fromExtras(mapOf("inputType" to "1")) }
    }
    @Test fun wrongBooleanTypeIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { CaseConfig.fromExtras(mapOf("performEditorActionReturnValue" to 0)) }
    }
    @Test fun wrongLabelTypeIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { CaseConfig.fromExtras(mapOf("customActionLabel" to 73)) }
    }
    @Test fun nonPositiveMinLinesIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { CaseConfig.fromExtras(mapOf("minLines" to 0)) }
    }
    @Test fun explicitNullIntIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { CaseConfig.fromExtras(mapOf("imeOptions" to null)) }
    }
    @Test fun initialTextIsObserved() {
        assertEquals("seed\n", EditorRecorder(CaseConfig(initialText = "seed\n")).snapshot.currentText)
    }
    @Test fun connectionRecordsActualPlatformAndFinalSeparately() {
        val recorder = EditorRecorder(CaseConfig(imeOptions = 4))
        val platform = EditorInfoSnapshot(1, 6, null, 0, true)
        val final = EditorInfoSnapshot(1, 4, "確認", 73, true)
        recorder.recordConnection(platform, final)
        assertEquals(platform, recorder.snapshot.platformMetadata)
        assertEquals(final, recorder.snapshot.finalMetadata)
    }
    @Test fun nullConnectionIsRepresentedHonestly() {
        val recorder = EditorRecorder(CaseConfig(inputType = 0))
        val actual = EditorInfoSnapshot(0, 0, null, 0, false)
        recorder.recordConnection(actual, actual)
        assertFalse(recorder.snapshot.finalMetadata!!.inputConnectionPresent)
    }
    @Test fun trueActionCountsExactIds() {
        val recorder = EditorRecorder(CaseConfig())
        assertTrue(recorder.recordAction(4))
        assertTrue(recorder.recordAction(73))
        assertEquals(2, recorder.snapshot.editorActionCount)
        assertEquals(73, recorder.snapshot.lastEditorActionId)
        assertEquals(true, recorder.snapshot.lastActionReturnValue)
    }
    @Test fun falseActionStillCountsAndDoesNotInsertText() {
        val recorder = EditorRecorder(CaseConfig(performEditorActionReturnValue = false))
        assertFalse(recorder.recordAction(4))
        assertEquals(1, recorder.snapshot.editorActionCount)
        assertEquals(4, recorder.snapshot.lastEditorActionId)
        assertEquals(false, recorder.snapshot.lastActionReturnValue)
        assertEquals("", recorder.snapshot.currentText)
        assertEquals(0, recorder.snapshot.newlineCount)
    }
    @Test fun customZeroActionCounts() {
        val recorder = EditorRecorder(CaseConfig())
        recorder.recordAction(0)
        assertEquals(0, recorder.snapshot.lastEditorActionId)
    }
    @Test fun newlineCountComesFromActualText() {
        val recorder = EditorRecorder(CaseConfig())
        recorder.recordText("a\nb\n\n")
        assertEquals(3, recorder.snapshot.newlineCount)
        recorder.recordText("ab")
        assertEquals(0, recorder.snapshot.newlineCount)
    }
    @Test fun carriageReturnIsNotInventedAsNewline() {
        assertEquals(1, EditorSnapshot("test", currentText = "a\r\nb\r").newlineCount)
    }
    @Test fun commitObservationDoesNotFakeEditableText() {
        val recorder = EditorRecorder(CaseConfig())
        recorder.recordCommit("你")
        assertEquals(1, recorder.snapshot.commitCount)
        assertEquals("你", recorder.snapshot.lastCommitText)
        assertEquals("", recorder.snapshot.currentText)
    }
    @Test fun snapshotsRemainImmutableAndAdvanceRevision() {
        val recorder = EditorRecorder(CaseConfig())
        val before = recorder.snapshot
        recorder.recordAction(4)
        recorder.recordText("synthetic")
        assertEquals(0, before.editorActionCount)
        assertEquals("", before.currentText)
        assertEquals(2, recorder.snapshot.revision)
    }
}
