package io.github.juhjuhx.kuyin.acceptancehost

import android.content.ComponentName
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.view.inputmethod.BaseInputConnection
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.AssumptionViolatedException

internal enum class VisibleKeyboardMode { ZHUYIN, ENGLISH, SYMBOLS, EMOJI }

internal data class VisiblePreedit(val text: String, val bounds: Rect, val clearBounds: Rect)

/**
 * Host 的 immutable snapshot 是 editor oracle；UIAutomator 只負責真可見操作。
 * 不切換系統 IME、不使用硬體 Enter、adb shell、native 或產品 in-process hook。
 */
internal class AcceptanceDriver(private val caseId: String) {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val device = UiDevice.getInstance(instrumentation)
    private var scenario: ActivityScenario<AcceptanceEditorActivity>? = null
    private val externalUi = ExternalUiObservation(device)
    private var observationDeadline: Long? = null
    val observations = JSONArray()
    var scope: String = "真 external editor metadata、可見軟鍵呈現與 action／newline／commit 增量。"

    fun inconclusive(reason: String, detail: String = reason): Nothing =
        throw CaseInconclusive(reason, detail)

    fun launch(config: CaseConfig, allowUnobservedNullBoundary: Boolean = false) {
        assertSelectedIme()
        close()
        val intent = Intent(context, AcceptanceEditorActivity::class.java).apply {
            putExtra("caseId", config.caseId)
            putExtra("inputType", config.inputType)
            putExtra("imeOptions", config.imeOptions)
            if (config.customActionLabel != null) putExtra("customActionLabel", config.customActionLabel)
            putExtra("customActionId", config.customActionId)
            putExtra("performEditorActionReturnValue", config.performEditorActionReturnValue)
            putExtra("initialText", config.initialText)
            putExtra("minLines", config.minLines)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        scenario = ActivityScenario.launch(intent)
        uniqueHost("editor").click()
        val metadataReady = poll {
            val value = snapshot()
            value.caseId == config.caseId && value.finalMetadata != null
        }
        if (!metadataReady) {
            observe("metadata-unavailable", snapshot())
            if (config.inputType == 0) {
                if (allowUnobservedNullBoundary) {
                    note("TYPE_NULL_BOUNDARY_UNOBSERVABLE", "TextView 未提供可觀測的 onCreateInputConnection callback；未製造假 IC。")
                    return
                }
                inconclusive("TYPE_NULL_BOUNDARY_UNOBSERVABLE")
            }
            throw AssertionError("${config.caseId} 沒有當次 final EditorInfo snapshot")
        }
        val ready = snapshot()
        assertMetadata(config, ready)
        assertEquals("新 editor 文字必須是宣告的 synthetic 初值", config.initialText, ready.currentText)
        assertEquals("新 editor 不得沿用上一 case action", 0, ready.editorActionCount)
        assertEquals("新 editor 不得沿用上一 case commit", 0, ready.commitCount)
        observe("editor-ready", ready)
        if (config.inputType == 0 && allowUnobservedNullBoundary) return
        if (!poll { clickableDescriptions("收起鍵盤").size == 1 }) {
            if (config.inputType == 0) inconclusive("TYPE_NULL_SOFT_INPUT_UNOBSERVABLE")
            blocked("未能取得 KU-Yin 可見鍵盤 marker「收起鍵盤」")
        }
        // Host callback 可先於 IME 的非同步 EditorInfo 更新。已宣告矩陣的真呈現
        // 是額外可見 readiness 證據，避免沿用上一 editor 的布局就判斷 restoration。
        val presentation = AcceptanceMatrix.enterCases.firstOrNull {
            it.config.imeOptions == config.imeOptions &&
                it.config.customActionLabel == config.customActionLabel &&
                it.config.customActionId == config.customActionId
        }
        if (presentation != null && !poll {
            if (presentation.expectedActionId != null) clickableTexts(presentation.expectedLabel).size == 1
            else clickableDescriptions(presentation.expectedLabel).size == 1
        }) blocked("當次 ${config.caseId} 的 EditorInfo 尚無唯一對應 Enter 呈現")
    }

    private fun assertSelectedIme() {
        val raw = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        val selected = raw?.let(ComponentName::unflattenFromString)
        val expected = ComponentName(PRODUCT_PACKAGE, "com.example.ime.KuYinInputMethodService")
        assertEquals("SETUP_IME_NOT_SELECTED：請依 runbook 先選取 KU-Yin；tests 不修改 default IME", expected, selected)
    }

    fun snapshot(): EditorSnapshot {
        var result: EditorSnapshot? = null
        checkNotNull(scenario) { "尚未啟動 host ActivityScenario" }.onActivity { result = it.editorSnapshot }
        return checkNotNull(result) { "host snapshot getter 沒有回傳" }
    }

    private fun assertMetadata(config: CaseConfig, value: EditorSnapshot) {
        assertEquals("當次 oracle caseId", config.caseId, value.caseId)
        assertNotNull("必須保存 super 後的 platform metadata", value.platformMetadata)
        val final = checkNotNull(value.finalMetadata)
        assertEquals("實際 final inputType", config.inputType, final.inputType)
        assertEquals("實際 final imeOptions", config.imeOptions, final.imeOptions)
        assertEquals("實際 final actionLabel", config.customActionLabel, final.actionLabel)
        assertEquals("實際 final actionId", config.customActionId, final.actionId)
        assertEquals("TYPE_NULL 必須保留 super 的真正 null IC", config.inputType != 0, final.inputConnectionPresent)
        assertEquals("platform/final 必須記錄同一真正 IC 存在性", value.platformMetadata?.inputConnectionPresent, final.inputConnectionPresent)
    }

    fun observe(name: String, value: EditorSnapshot) {
        observations.put(JSONObject().put("name", name).put("editor", value.toEvidence()))
    }

    fun note(name: String, value: String) {
        observations.put(JSONObject().put("name", name).put("observation", value))
    }

    fun selectMode(mode: VisibleKeyboardMode) {
        val current = awaitVisibleMode()
        if (current != mode) {
            val switchLabel = when (mode) {
                VisibleKeyboardMode.ZHUYIN -> if (current == VisibleKeyboardMode.EMOJI) "返回注音" else "注音"
                VisibleKeyboardMode.ENGLISH -> when (current) {
                    VisibleKeyboardMode.ZHUYIN -> "EN"
                    VisibleKeyboardMode.SYMBOLS -> "ABC"
                    VisibleKeyboardMode.EMOJI -> {
                        tapText("返回注音")
                        awaitMode(VisibleKeyboardMode.ZHUYIN)
                        "EN"
                    }
                    else -> error("英文模式已選取")
                }
                VisibleKeyboardMode.SYMBOLS -> {
                    if (current == VisibleKeyboardMode.EMOJI) {
                        tapText("返回注音")
                        awaitMode(VisibleKeyboardMode.ZHUYIN)
                    }
                    "?123"
                }
                VisibleKeyboardMode.EMOJI -> {
                    if (current != VisibleKeyboardMode.ZHUYIN) selectMode(VisibleKeyboardMode.ZHUYIN)
                    "😊"
                }
            }
            tapText(switchLabel)
        }
        awaitMode(mode)
        note("visible-mode", mode.name)
    }

    private fun visibleMode(): VisibleKeyboardMode? {
        val tree = captureUi().tree
        fun has(label: String) = tree.textTargets(label).size == 1
        val modes = buildList {
            if (has("EN") && has("?123") && has("空白 (一聲)")) add(VisibleKeyboardMode.ZHUYIN)
            if (has("注音") && has("?123") && has("Space")) add(VisibleKeyboardMode.ENGLISH)
            if (has("注音") && has("ABC") && has("空白")) add(VisibleKeyboardMode.SYMBOLS)
            if (has("返回注音")) add(VisibleKeyboardMode.EMOJI)
        }
        return modes.singleOrNull()
    }

    private fun awaitVisibleMode(): VisibleKeyboardMode {
        var observed: VisibleKeyboardMode? = null
        if (!poll { observed = visibleMode(); observed != null }) blocked("無法以唯一可見功能鍵辨識布局")
        return checkNotNull(observed)
    }

    private fun awaitMode(mode: VisibleKeyboardMode) {
        if (!poll { visibleMode() == mode }) {
            val actual = visibleMode()
            if (actual != null) throw AssertionError("可見布局應為 ${mode.name}，實際為 ${actual.name}")
            blocked("等待可見布局 ${mode.name} 逾時；無法唯一辨識實際布局")
        }
    }

    fun assertEmptyComposition() {
        assertTrue("空組字不得存在可見清除 marker", clickableDescriptions("清除").isEmpty())
    }

    fun tapPhonetic(glyph: String) {
        val before = strictPreedit()?.text
        uniqueClickable("phonetic:$glyph") { clickableTexts(glyph, tokenMatch = true) }.click()
        var lastCompleteObservation: VisibleUiSnapshot? = null
        if (!poll {
            val tree = captureUi().tree
            lastCompleteObservation = tree
            tree.strictPreedit()?.text?.let { it != before } == true
        }) {
            // Diagnose the last completed immutable observation. Do not make an unguarded
            // accessibility read after the deadline or mistake a partial tree for empty UI.
            val tree = lastCompleteObservation ?: blocked("點擊 $glyph 後沒有完整 fresh UI observation；preedit timeout 原因未證明")
            val actual = tree.strictPreedit()?.text
            if (actual != null) throw AssertionError("真可見按鍵 $glyph 未更新 preedit：$actual")
            if (tree.descriptionTargets("清除").isEmpty()) throw AssertionError("真可見按鍵 $glyph 未建立組字 marker")
            blocked("點擊 $glyph 後未取得當次可辨識 preedit 變化")
        }
    }

    private fun tapText(label: String) {
        uniqueClickable("text:$label") { clickableTexts(label) }.click()
    }

    fun compose(glyphs: List<String>) {
        for (glyph in glyphs) {
            tapPhonetic(glyph)
            awaitPreedit()
        }
        observe("after-visible-composition", snapshot())
    }

    /** 清除 marker ＋最小可辨識同 subtree 中、無 clickable ancestor 的文字 leaf。 */
    fun awaitPreedit(): VisiblePreedit {
        var found: VisiblePreedit? = null
        if (!poll { found = strictPreedit(); found != null }) {
            blocked("清除 marker 旁缺少唯一 non-clickable preedit leaf；鍵面 glyph 不作組字證據")
        }
        val value = checkNotNull(found)
        observations.put(JSONObject().put("name", "strict-preedit")
            .put("text", value.text).put("bounds", value.bounds.toShortString())
            .put("clear_bounds", value.clearBounds.toShortString()))
        return value
    }

    private fun strictPreedit(): VisiblePreedit? = captureUi().tree.strictPreedit()?.let {
        VisiblePreedit(it.text, it.bounds.toRect(), it.clearBounds.toRect())
    }

    fun clearComposition() {
        val clear = clickableDescriptions("清除")
        if (clear.isEmpty()) return
        uniqueClickable("description:清除") { clickableDescriptions("清除") }.click()
        if (!poll { clickableDescriptions("清除").isEmpty() }) blocked("clear 後組字 marker 未消失")
    }

    private fun enterKey(case: EnterCase): VisibleTarget {
        val isAction = case.expectedActionId != null
        if (case.customPresentation && isAction && case.expectedLabel == "換行") {
            assertTrue("自訂『換行』action 不得使用 newline icon description", clickableDescriptions("換行").isEmpty())
        }
        if (!isAction) {
            assertTrue("newline 必須呈現 return icon，不能是文字 action", clickableTexts(case.expectedLabel).isEmpty())
        }
        val key = if (isAction) {
            uniqueClickable("action-text:${case.expectedLabel}") { clickableTexts(case.expectedLabel) }
        } else {
            uniqueClickable("newline-description:${case.expectedLabel}") { clickableDescriptions(case.expectedLabel) }
        }
        note("enter-presentation", if (isAction) "Text=${case.expectedLabel}" else "contentDescription=${case.expectedLabel}")
        return key
    }

    fun assertEmptyEnter(case: EnterCase) {
        assertEmptyComposition()
        val key = enterKey(case)
        val before = snapshot()
        observe("empty-enter-baseline", before)
        key.click()
        val after = awaitEditorEffect(before)
        if (case.expectedActionId == null) {
            assertEquals("一次真 newline 不得呼叫 action", before.editorActionCount, after.editorActionCount)
            assertEquals("newline 必須恰好增加一次", before.newlineCount + 1, after.newlineCount)
            assertEquals("newline 必須只附加一個 LF", before.currentText + "\n", after.currentText)
            assertEquals("newline 必須恰好 commitText 一次", before.commitCount + 1, after.commitCount)
            assertEquals("newline commitText 的實際內容", "\n", after.lastCommitText)
            assertEquals("newline 不得改寫上一 actionId", before.lastEditorActionId, after.lastEditorActionId)
        } else {
            assertEquals("一次 action 不得重複派送", before.editorActionCount + 1, after.editorActionCount)
            assertEquals("真 action id", case.expectedActionId, after.lastEditorActionId)
            assertEquals("host 記錄實際配置 return boolean", case.config.performEditorActionReturnValue, after.lastActionReturnValue)
            assertEquals("action 不得 fallback newline", before.newlineCount, after.newlineCount)
            assertEquals("action 不得修改 editor 文字", before.currentText, after.currentText)
            assertEquals("action 不得呼叫 commitText", before.commitCount, after.commitCount)
        }
        observe("empty-enter-after", after)
    }

    fun assertCompositionEnter(case: EnterCase, performSecondEnter: Boolean = true) {
        awaitPreedit()
        val key = enterKey(case)
        val boundary = hostCompositionBoundary()
        val before = boundary.editor
        observe("composition-enter-baseline", before)
        key.click()
        val after = awaitEditorEffect(before)
        assertCompositionCompletion(boundary, after)
        if (!poll { clickableDescriptions("清除").isEmpty() }) blocked("完成 Enter 後組字 marker 未消失")
        assertCompositionCompletion(boundary, settledSnapshot())
        val completedBoundary = hostCompositionBoundary()
        assertCompositionCompletion(boundary, completedBoundary.editor)
        assertEquals("完成 Enter 後真 host 不得保留 composing span start", -1, completedBoundary.start)
        assertEquals("完成 Enter 後真 host 不得保留 composing span end", -1, completedBoundary.end)
        assertEmptyComposition()
        observe("composition-enter-after", completedBoundary.editor)
        if (performSecondEnter) assertEmptyEnter(case)
    }

    private fun hostCompositionBoundary(): EditorCompositionBoundary {
        var result: EditorCompositionBoundary? = null
        checkNotNull(scenario).onActivity { activity ->
            val editor = activity.findViewById<RecordingEditText>(R.id.editor)
            val text = editor.text
            val current = activity.editorSnapshot
            assertEquals("boundary 必須來自同次真 host Editable", current.currentText, text.toString())
            result = EditorCompositionBoundary(current, BaseInputConnection.getComposingSpanStart(text),
                BaseInputConnection.getComposingSpanEnd(text))
        }
        val boundary = checkNotNull(result)
        observations.put(JSONObject().put("name", "actual-host-composition-boundary")
            .put("revision", boundary.editor.revision).put("start", boundary.start).put("end", boundary.end))
        return boundary
    }

    fun assertThreeLayoutEnter(case: EnterCase) {
        for (mode in listOf(VisibleKeyboardMode.ZHUYIN, VisibleKeyboardMode.ENGLISH, VisibleKeyboardMode.SYMBOLS)) {
            selectMode(mode)
            assertEmptyEnter(case)
            observe("${mode.name}-enter", snapshot())
        }
        // 也驗證 Symbols → ABC 與 English → 注音，而非假設僅單向切換。
        selectMode(VisibleKeyboardMode.ENGLISH)
        selectMode(VisibleKeyboardMode.ZHUYIN)
    }

    fun assertNonTextPresentation(attemptZhuyin: Boolean) {
        awaitMode(VisibleKeyboardMode.ENGLISH)
        assertEmptyComposition()
        assertFalse("temporary English 不得呈現注音鍵", clickableTexts("ㄅ", tokenMatch = true).isNotEmpty())
        if (attemptZhuyin) {
            val before = snapshot()
            tapText("注音")
            // 對禁止事件採短而有界的觀測窗；每次都重新讀可見布局與 marker。
            val deadline = SystemClock.uptimeMillis() + QUIET_WINDOW_MS
            do {
                assertEquals("非 text editor 的注音切換必須遭 policy 阻擋", VisibleKeyboardMode.ENGLISH, visibleMode())
                assertEmptyComposition()
                assertFalse("嘗試切換仍不得出現注音鍵", clickableTexts("ㄅ", tokenMatch = true).isNotEmpty())
                SystemClock.sleep(POLL_INTERVAL_MS)
            } while (SystemClock.uptimeMillis() < deadline)
            val after = settledSnapshot()
            assertEquals("mode attempt 不得提交文字", before.commitCount, after.commitCount)
            assertEquals("mode attempt 不得派送 action", before.editorActionCount, after.editorActionCount)
            assertEquals("mode attempt 不得改 editor 文字", before.currentText, after.currentText)
            observe("non-text-zhuyin-attempt", after)
        }
        note("non-text-visible-policy", "temporary ENGLISH；無清除 marker／注音鍵")
    }

    fun assertPreferredModeRestoration() {
        for (preferred in listOf(VisibleKeyboardMode.ZHUYIN, VisibleKeyboardMode.ENGLISH)) {
            launch(CaseConfig(caseId = "C05-${preferred.name}-設定", imeOptions = 4))
            selectMode(preferred)
            assertEmptyComposition()
            for (nonText in AcceptanceMatrix.nonTextCases) {
                val transition = nonText.copy(caseId = "C05-${preferred.name}-${nonText.caseId}")
                launch(transition, allowUnobservedNullBoundary = transition.inputType == 0)
                if (transition.inputType == 0) {
                    observe("TYPE_NULL-transition", snapshot())
                    note("TYPE_NULL-policy-gap", "editor 的真 null IC 不代表 IME 端無 fallback IC；不從舊鍵盤推定當次 non-text policy。")
                } else {
                    // 不點『注音』；那個 user callback 會覆寫 preferred mode，破壞本 case 前提。
                    assertNonTextPresentation(attemptZhuyin = false)
                }
                launch(CaseConfig(caseId = "C05-${preferred.name}-${nonText.caseId}-返回text", imeOptions = 4))
                awaitMode(preferred)
                assertEmptyComposition()
                note("preferred-restored", "${nonText.caseId} → text：${preferred.name}")
                observe("preferred-restoration", snapshot())
            }
        }
    }

    fun assertCanonicalDachenKeys() {
        val rows = listOf(
            listOf("ㄅ" to "1", "ㄉ" to "2", "ˇ" to "3", "ˋ" to "4", "ㄓ" to "5", "ˊ" to "6", "˙" to "7", "ㄚ" to "8", "ㄞ" to "9", "ㄢ" to "0", "ㄦ" to "-"),
            listOf("ㄆ" to "q", "ㄊ" to "w", "ㄍ" to "e", "ㄐ" to "r", "ㄔ" to "t", "ㄗ" to "y", "ㄧ" to "u", "ㄛ" to "i", "ㄟ" to "o", "ㄣ" to "p"),
            listOf("ㄇ" to "a", "ㄋ" to "s", "ㄎ" to "d", "ㄑ" to "f", "ㄕ" to "g", "ㄘ" to "h", "ㄨ" to "j", "ㄜ" to "k", "ㄠ" to "l", "ㄤ" to ";"),
            listOf("ㄈ" to "z", "ㄌ" to "x", "ㄏ" to "c", "ㄒ" to "v", "ㄖ" to "b", "ㄙ" to "n", "ㄩ" to "m", "ㄝ" to ",", "ㄡ" to ".", "ㄥ" to "/"),
        )
        val seenBounds = mutableSetOf<String>()
        var priorRowY = -1
        for ((rowIndex, row) in rows.withIndex()) {
            var priorX = -1
            var rowY: Int? = null
            for ((glyph, hint) in row) {
                val key = uniqueClickable("canonical:$glyph") { clickableTexts(glyph, tokenMatch = true) }
                val texts = key.texts()
                if (texts.none { semanticToken(it, hint) }) blocked("$glyph 的 ASCII 提示 $hint 無法由可見 hierarchy 唯一驗證")
                val bounds = key.visibleBounds
                assertTrue("41 glyph 不得重用同一 hitbox", seenBounds.add(bounds.toShortString()))
                assertTrue("canonical 第 ${rowIndex + 1} 排需保持從左至右", bounds.centerX() > priorX)
                if (rowY != null) assertEquals("同排鍵中心高度", rowY, bounds.centerY())
                rowY = bounds.centerY()
                priorX = bounds.centerX()
            }
            assertTrue("canonical 四排需保持從上至下", checkNotNull(rowY) > priorRowY)
            priorRowY = checkNotNull(rowY)
        }
        assertEquals("canonical phonetic／tone 可見鍵數", 41, seenBounds.size)
        note("canonical-dachen", "41 unique hitboxes；四排順序與 ASCII hint 已核對；displayRotation=${device.displayRotation}")
    }

    fun tapFirstVisibleCandidate(committedPrefix: String): String = completeCandidate(null) { committedPrefix + it }

    fun tapCompletionCandidate(label: String, expectedEditorText: String) {
        completeCandidate(label) { expectedEditorText }
    }

    private fun completeCandidate(label: String?, expectedText: (String) -> String): String {
        awaitPreedit()
        val before = snapshot()
        val target = uniqueClickable("candidate:${label ?: "first-visible"}") {
            val captured = captureUi()
            val preedit = captured.tree.strictPreedit()
            val candidates = if (preedit == null) emptyList() else captured.tree.candidates(preedit)
            val matches = if (label != null) candidates.filter { captured.tree.targetText(it) == label }
                else {
                    val first = candidates.minWithOrNull(compareBy<VisibleUiNode> { it.bounds.top }.thenBy { it.bounds.left })
                    candidates.filter { it.bounds == first?.bounds }
                }
            captured.targets(matches)
        }
        val selected = target.tree.targetText(target.node) ?: blocked("candidate 沒有唯一完整文字")
        note("visible-candidate-tap", selected)
        target.click()
        val after = awaitEditorEffect(before)
        assertCandidateCompletion(before, after, selected, expectedText(selected))
        if (!poll { clickableDescriptions("清除").isEmpty() }) blocked("candidate completion 後組字 marker 未消失")
        val settled = settledSnapshot()
        assertCandidateCompletion(before, settled, selected, expectedText(selected))
        assertEmptyComposition()
        observe("completion-$selected", settled)
        return selected
    }

    fun tapEnglishSoftKey(letter: String) {
        val target = uniqueClickable("english-key:$letter") {
            val captured = captureUi()
            val top = captured.tree.keyboardTop()
            captured.targets(if (top == null) emptyList() else captured.tree.textTargets(letter).filter { it.bounds.top >= top })
        }
        val before = snapshot().currentText
        target.click()
        if (!poll(timeoutMs = 8_000) { snapshot().currentText != before })
            throw AssertionError("visible English key $letter did not update editor composition")
    }

    private fun awaitEditorEffect(before: EditorSnapshot): EditorSnapshot {
        val changed = poll {
            val after = snapshot()
            after.editorActionCount != before.editorActionCount || after.commitCount != before.commitCount ||
                after.currentText != before.currentText || after.newlineCount != before.newlineCount
        }
        assertTrue("可見軟鍵點擊後須有當次 editor effect", changed)
        return settledSnapshot()
    }

    /** 等待 side effects 安定；短 polling 驗證重複 effect，並非 sleep 後假定完成。 */
    fun settledSnapshot(): EditorSnapshot {
        var last = snapshot()
        var quietSince = SystemClock.uptimeMillis()
        val settled = poll(timeoutMs = SETTLE_TIMEOUT_MS) {
            val next = snapshot()
            if (next.effectKey() != last.effectKey()) quietSince = SystemClock.uptimeMillis()
            last = next
            SystemClock.uptimeMillis() - quietSince >= QUIET_WINDOW_MS
        }
        assertTrue("editor effect 在有界觀測窗內應安定", settled)
        return last
    }

    fun close() {
        val active = scenario ?: return
        try {
            clearComposition()
        } finally {
            scenario = null
            active.close()
        }
    }

    private fun uniqueHost(id: String): UiObject2 {
        var matches = emptyList<UiObject2>()
        if (!poll { matches = device.findObjects(By.res(HOST_PACKAGE, id)).filter {
            it.visibleBounds.run { width() > 0 && height() > 0 }
        }; matches.size == 1 }) blocked("host:$id 可見匹配數=${matches.size}，要求恰好 1")
        return matches.single()
    }

    private fun uniqueClickable(label: String, query: () -> List<VisibleTarget>): VisibleTarget {
        var matches = emptyList<VisibleTarget>()
        if (!poll { matches = query(); matches.size == 1 }) blocked("$label 可見匹配數=${matches.size}，要求恰好 1")
        val result = matches.single()
        if (!result.isClickable) blocked("$label 沒有可辨識的 clickable hitbox")
        return result
    }

    private fun clickableTexts(label: String, tokenMatch: Boolean = false): List<VisibleTarget> {
        val captured = captureUi()
        return captured.targets(captured.tree.textTargets(label, tokenMatch))
    }

    private fun clickableDescriptions(label: String): List<VisibleTarget> {
        val captured = captureUi()
        return captured.targets(captured.tree.descriptionTargets(label))
    }

    private fun captureUi(): CapturedUi = try {
        externalUi.capture(observationDeadline ?: (SystemClock.uptimeMillis() + UI_TIMEOUT_MS))
    } catch (error: UiReadDeadline) {
        if (observationDeadline != null) throw error
        blocked("fresh UI observation 未在有界 deadline 完成；沒有使用部分或過時 observation 判定結果")
    }

    private fun blocked(detail: String): Nothing = inconclusive("BLOCKED_SELECTOR", detail)

    private fun poll(timeoutMs: Long = UI_TIMEOUT_MS, predicate: () -> Boolean): Boolean {
        val previous = observationDeadline
        val deadline = minOf(previous ?: Long.MAX_VALUE, SystemClock.uptimeMillis() + timeoutMs)
        observationDeadline = deadline
        try {
            do {
                try {
                    if (predicate()) return SystemClock.uptimeMillis() <= deadline
                } catch (_: StaleObjectException) {
                    // Host metadata read only; gestures are never executed in a predicate.
                } catch (_: UiReadDeadline) {
                    // A prior completed tree cannot diagnose the current preedit after an
                    // unobservable tail. Preserve this read gap instead of asserting a decoder fault.
                    blocked("fresh UI observation 未在當次 polling deadline 完成；timeout 根因未證明")
                }
                val remaining = deadline - SystemClock.uptimeMillis()
                if (remaining <= 0) return false
                SystemClock.sleep(minOf(POLL_INTERVAL_MS, remaining))
            } while (true)
        } finally {
            observationDeadline = previous
        }
    }

    fun evidence(reason: String?, error: Throwable?): JSONObject = JSONObject()
        .put("device_model", Build.MODEL)
        .put("api", Build.VERSION.SDK_INT)
        .put("abi", JSONArray(Build.SUPPORTED_ABIS.toList()))
        .put("display_rotation", device.displayRotation)
        .put("reason", reason ?: JSONObject.NULL)
        .put("error", error?.message ?: JSONObject.NULL)
        .put("error_details", error?.toFailureEvidence() ?: JSONObject.NULL)
        .put("observations", observations)

    companion object {
        const val HOST_PACKAGE = "io.github.juhjuhx.kuyin.acceptancehost"
        const val PRODUCT_PACKAGE = "io.github.juhjuhx.kuyin"
        private const val UI_TIMEOUT_MS = 5_000L
        private const val SETTLE_TIMEOUT_MS = 2_000L
        private const val QUIET_WINDOW_MS = 300L
        private const val POLL_INTERVAL_MS = 50L
    }
}

/** 一個 case 只 emit 一個 envelope；JUnit skip 絕不能由此轉成 PASS。 */
internal fun acceptanceCase(caseId: String, block: AcceptanceDriver.() -> Unit) {
    val driver = AcceptanceDriver(caseId)
    var failure: Throwable? = null
    try {
        driver.block()
    } catch (error: Throwable) {
        failure = error
    }
    try {
        driver.close()
    } catch (cleanupError: Throwable) {
        failure = mergeCaseFailure(failure, cleanupError)
    }
    val result = when {
        failure == null -> "PASS"
        failure.isInconclusive() -> "INCONCLUSIVE"
        else -> "FAIL"
    }
    val reason = when (val error = failure) {
        is CaseInconclusive -> error.reason
        is AssumptionViolatedException -> "JUNIT_ASSUMPTION"
        null -> null
        else -> if (error.message?.contains("SETUP_IME_NOT_SELECTED") == true) "SETUP_IME_NOT_SELECTED" else "ASSERTION_OR_RUNTIME_FAILURE"
    }
    val envelope = JSONObject().put("case_id", caseId).put("result", result)
        .put("evidence", driver.evidence(reason, failure)).put("scope", driver.scope)
    val status = Bundle().apply {
        putString("CASE_ID", caseId)
        putString("RESULT", result)
        if (reason != null) putString("REASON", reason)
        putString("stream", "P1_ACCEPTANCE_CASE=$envelope\n")
    }
    InstrumentationRegistry.getInstrumentation().sendStatus(2, status)
    when (val error = failure) {
        null -> Unit
        is CaseInconclusive -> throw AssumptionViolatedException("$caseId RESULT=INCONCLUSIVE REASON=${error.reason}: ${error.message}", error)
        else -> throw error
    }
}

private fun EditorSnapshot.effectKey(): List<Any?> = listOf(
    editorActionCount, lastEditorActionId, lastActionReturnValue, currentText, newlineCount, commitCount, lastCommitText,
)

private fun EditorSnapshot.toEvidence(): JSONObject = JSONObject()
    .put("case_id", caseId).put("revision", revision)
    .put("platform_metadata", platformMetadata?.toEvidence() ?: JSONObject.NULL)
    .put("final_metadata", finalMetadata?.toEvidence() ?: JSONObject.NULL)
    .put("editor_action_count", editorActionCount)
    .put("last_editor_action_id", lastEditorActionId ?: JSONObject.NULL)
    .put("last_action_return_value", lastActionReturnValue ?: JSONObject.NULL)
    .put("current_text", currentText).put("newline_count", newlineCount)
    .put("commit_count", commitCount).put("last_commit_text", lastCommitText ?: JSONObject.NULL)

private fun EditorInfoSnapshot.toEvidence(): JSONObject = JSONObject()
    .put("input_type", inputType).put("ime_options", imeOptions)
    .put("action_label", actionLabel ?: JSONObject.NULL).put("action_id", actionId)
    .put("input_connection_present", inputConnectionPresent)

private fun Throwable.toFailureEvidence(depth: Int = 0): JSONObject {
    val detail = JSONObject().put("type", javaClass.name).put("message", message ?: JSONObject.NULL)
    if (depth < 4) {
        detail.put("suppressed", JSONArray(suppressed.map { it.toFailureEvidence(depth + 1) }))
        detail.put("cause", cause?.toFailureEvidence(depth + 1) ?: JSONObject.NULL)
    }
    return detail
}
