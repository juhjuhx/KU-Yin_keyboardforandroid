import com.example.androidkeyboard.engines.android.AndroidChewingEngine
import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate

private fun json(s: String) = "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""
private fun json(u: EngineUpdate): String = "{\"consumed\":${u.consumed},\"preedit\":${json(u.preedit)},\"candidates\":${u.candidates.joinToString(prefix="[",postfix="]") { json(it) }},\"committedText\":${json(u.committedText)},\"unavailableReason\":${u.unavailableReason?.let { json(it) } ?: "null"}}"
private fun actualKeys(e: AndroidChewingEngine): List<Int> {
    val field=e.javaClass.getDeclaredField("nativeActions").also { it.isAccessible=true }
    return (field.get(e) as List<*>).filterNotNull().filter { it.javaClass.simpleName=="Key" }
        .map { it.javaClass.getDeclaredMethod("getCode").also { method -> method.isAccessible=true }.invoke(it) as Int }
}
private fun sha256(file: java.io.File): String = java.security.MessageDigest.getInstance("SHA-256")
    .digest(file.readBytes()).joinToString("") { "%02x".format(it) }

fun main() {
    val dictionary = "/private/tmp/kuyin-p1g-native-20261009/fcitx5-android-prebuilt/chewing-dict"
    val failures = mutableListOf<String>()
    // Real CAPI inserts one fixed supplementary-plane word into a :memory:
    // dictionary; all reads still call the production JNI output wrappers.
    AndroidChewingEngine(dictionary, ":memory:").also { e ->
        e.init(); check(e.isReady); e.setPersonalizedLearningEnabled(false)
        val ctx = e.javaClass.getDeclaredField("nativeCtx").also { it.isAccessible = true }.getLong(e)
        check(NativeUnicodeFixture.addWord(ctx) == 1)
        "ru6".forEach { e.handleKeyUpdate(it.code) }
        fun call(name: String, vararg arguments: Any): Any? {
            val types = arguments.map { if (it is Long) java.lang.Long.TYPE else java.lang.Integer.TYPE }.toTypedArray()
            return e.javaClass.getDeclaredMethod(name, *types).also { it.isAccessible = true }.invoke(e, *arguments)
        }
        call("chewing_cand_open", ctx)
        val choices = call("chewing_cand_strings", ctx) as Array<*>
        val index = choices.indexOf("𠮷")
        check(index >= 0) { "Production JNI must preserve supplementary UTF-8 candidate: ${choices.toList()}" }
        check(call("chewing_cand_string_by_index_static", ctx, index) == "𠮷")
        check(call("chewing_cand_choose_by_index", ctx, index) == 0)
        check(call("chewing_buffer_string_static", ctx) == "𠮷")
        check(call("chewing_commit_preedit_buf", ctx) == 0)
        check(call("chewing_commit_string_static", ctx) == "𠮷")
        e.close()
    }
    val cases = listOf("ji" to "我", "ji3vu04y94" to "我現在", "jivu0y9" to "我現在")
    val logs = cases.map { (sequence, target) ->
        val engine = AndroidChewingEngine(dictionary, ":memory:")
        engine.init(ChewingEngine.Layout.DACHEN)
        check(engine.isReady) { "True production JNI adapter must initialize" }
        engine.setPersonalizedLearningEnabled(false)
        val steps = sequence.map { key -> key to engine.handleKeyUpdate(key.code) }
        check(actualKeys(engine)==sequence.map { it.code }) { "Preview must not modify actual key identity" }
        val update = steps.last().second
        val index = update.candidates.indexOf(target)
        if (index < 0) failures.add("$sequence must offer authentic decoded candidate $target")
        val selected = engine.selectCandidateUpdate(index)
        val committed = engine.commitUpdate()
        if (selected.committedText + committed.committedText != target) failures.add("$sequence selection + commit must emit full phrase $target")
        val repeatedCommit = engine.commitUpdate()
        if (repeatedCommit.committedText.isNotEmpty()) failures.add("$sequence repeat commit must not duplicate text")
        engine.reset()
        val cleared = engine.backspaceUpdate()
        engine.close()
        "{\"sequence\":${json(sequence)},\"target\":${json(target)},\"steps\":[${steps.joinToString { (key,u) -> "{\"key\":${json(key.toString())},\"update\":${json(u)}}" }}],\"candidateIndex\":$index,\"selected\":${json(selected)},\"committed\":${json(committed)},\"repeatedCommit\":${json(repeatedCommit)},\"afterReset\":${json(cleared)}}"
    }
    fun engine(user: String = ":memory:") = AndroidChewingEngine(dictionary, user).also {
        it.init(); check(it.isReady); it.setPersonalizedLearningEnabled(false)
    }
    fun feed(e: AndroidChewingEngine, keys: String): EngineUpdate = keys.map { e.handleKeyUpdate(it.code) }.last()
    engine().use { e ->
        feed(e, "ji3")
        repeat(100) { e.handleKeyUpdate('v'.code); e.backspaceUpdate() }
        val actions = e.javaClass.getDeclaredField("nativeActions").also { it.isAccessible = true }.get(e) as List<*>
        check(actions.size == 3) { "Cancelled pending key/backspace must not grow replay history: ${actions.size}" }
        check(e.commitUpdate().committedText == "我")
    }
    val extra = mutableListOf<String>()
    fun record(name: String, value: EngineUpdate) { extra += "{\"name\":${json(name)},\"update\":${json(value)}}" }
    // A matching visible checkpoint is not a claim about all Rust internals.
    // Compare subsequent real decoding, candidate paging/selection and commits
    // with an independent context that never received the cancelled keys.
    for ((prefix, cancelled, suffix) in listOf(
        Triple("ji3", 'v', "vu04y94"),
        Triple("ji3v", 'u', "u04y94"),
        Triple("ji3vu", '0', "04y94"),
    )) {
        engine().use { clean -> engine().use { noisy ->
            val cleanBefore = feed(clean, prefix)
            feed(noisy, prefix)
            repeat(100) {
                noisy.handleKeyUpdate(cancelled.code)
                val afterBackspace = noisy.backspaceUpdate()
                check(afterBackspace == cleanBefore) { "Cancelled key must preserve observable decoding for $prefix" }
            }
            suffix.forEach { key ->
                check(noisy.handleKeyUpdate(key.code) == clean.handleKeyUpdate(key.code)) {
                    "Replay compaction must preserve subsequent decoding at $prefix/$key"
                }
            }
            check(noisy.backspaceUpdate() == clean.backspaceUpdate())
            "y94".forEach { key -> check(noisy.handleKeyUpdate(key.code) == clean.handleKeyUpdate(key.code)) }
            check(noisy.nextPageUpdate() == clean.nextPageUpdate())
            check(noisy.prevPageUpdate() == clean.prevPageUpdate())
            check(noisy.selectCandidateUpdate(1) == clean.selectCandidateUpdate(1))
            val noisyCommit = noisy.commitUpdate()
            check(noisyCommit == clean.commitUpdate())
            check(noisyCommit.committedText.isNotEmpty())
            check(noisy.commitUpdate() == clean.commitUpdate())
            record("cancel-replay-equivalence-$prefix", noisyCommit)
        } }
    }
    engine().use { e ->
        val before = feed(e, "jivu0y9")
        val selected = e.selectCandidateUpdate(1)
        record("tail-alternative", selected)
        check(selected.preedit == before.candidates[1]) { "Alternative must be full native selected buffer" }
        check(e.commitUpdate().committedText == before.candidates[1])
    }
    engine().use { e ->
        feed(e, "jivu0y9")
        val back = e.backspaceUpdate();record("backspace-tail",back)
        check(back.preedit == "我限ㄗ")
        val resumed=e.handleKeyUpdate('9'.code);record("backspace-resumed",resumed)
        check("我現在" in resumed.candidates);check(e.commitUpdate().committedText=="我現在")
    }
    engine().use { e ->
        feed(e,"jiji");val u=e.commitUpdate();record("repeated-keys",u)
        // The independent CAPI reference decodes repeated ji as 喔喔.
        check(u.committedText=="喔喔")
    }
    engine().use { e ->
        feed(e,"ji34");val u=e.commitUpdate();record("invalid-tone-tail",u)
        check(u.committedText.isEmpty() && u.preedit=="我ˋ")
        e.backspaceUpdate();check(e.commitUpdate().committedText=="我")
    }
    engine().use { e ->
        val u=feed(e,"jiv");record("partial-native-prediction",u)
        check("我想" in u.candidates)
        check(u.candidates.none { text -> text.any { it in '\u3100'..'\u312f' || it in "ˇˉˊˋ˙" } })
    }
    engine().use { e ->
        val before=feed(e,"1j0")
        val attempted=e.commitUpdate();record("invalid-syllable-commit",attempted)
        check(attempted.consumed && attempted.committedText.isEmpty() && attempted.unavailableReason != null)
        check(attempted.preedit==before.preedit && attempted.preedit=="ㄅㄨㄢ")
        check(actualKeys(e)=="1j0".map { it.code })
    }
    engine().use { e ->
        feed(e,"su3");val selected=e.selectCandidateUpdate(0);record("old-case4-selected",selected)
        check(selected.preedit.isNotEmpty() && selected.committedText.isEmpty())
        val continued=e.handleKeyUpdate('1'.code);record("old-case4-continued",continued)
        check(continued.consumed && continued.preedit.startsWith(selected.preedit))
        check(!e.personalizedLearningEnabled);e.setPersonalizedLearningEnabled(true);check(e.personalizedLearningEnabled)
    }
    engine().use { e ->
        val sequence="ji3vu04y94".repeat(15)
        val emitted=StringBuilder()
        var prunedPrefixes=0
        sequence.forEachIndexed { index, key ->
            val update=e.handleKeyUpdate(key.code)
            emitted.append(update.committedText)
            if (update.committedText.isNotEmpty()) {
                check(actualKeys(e).size < index+1) { "Already committed prefix must leave replay history" }
                prunedPrefixes++
            }
        }
        val final=e.commitUpdate();emitted.append(final.committedText)
        record("overflow-final",final)
        extra += "{\"name\":\"overflow-stream\",\"sequenceKeys\":${sequence.length},\"text\":${json(emitted.toString())}}"
        check(emitted.toString()=="我現在".repeat(15)) { "Overflow must emit each phrase exactly once: $emitted" }
        check(prunedPrefixes>0)
        check(e.commitUpdate().committedText.isEmpty())
    }
    engine().use { e ->
        val sequence="jivu0y9".repeat(15)
        val emitted=StringBuilder()
        sequence.forEach { emitted.append(e.handleKeyUpdate(it.code).committedText) }
        emitted.append(e.commitUpdate().committedText)
        check(emitted.toString()=="我現在".repeat(15)) { "No-tone long sentence must match independent CAPI" }
        extra += "{\"name\":\"no-tone-long-stream\",\"sequenceKeys\":${sequence.length},\"text\":${json(emitted.toString())}}"
    }
    val evidence=java.nio.file.Path.of(System.getProperty("p0r.evidence"))
    val personal=java.nio.file.Files.createTempDirectory(evidence,"personalized-").resolve("userdict.dat").toFile()
    val personalEngine=engine(personal.absolutePath)
    val hashBefore=sha256(personal)
    feed(personalEngine,"su3cl3")
    val hashAfterPreview=sha256(personal)
    check(hashBefore==hashAfterPreview) { "Preview must not modify persistent dictionary" }
    personalEngine.setPersonalizedLearningEnabled(true)
    check(personalEngine.personalizedLearningEnabled)
    val learned=personalEngine.commitUpdate();check(learned.committedText=="你好")
    personalEngine.close()
    val hashAfterCommit=sha256(personal)
    check(hashAfterCommit!=hashBefore) { "Live native commit must still persist personalized learning" }
    extra += "{\"name\":\"personalized-learning\",\"userdict\":${json(personal.absolutePath)},\"before\":${json(hashBefore)},\"afterPreview\":${json(hashAfterPreview)},\"afterLearnedCommit\":${json(hashAfterCommit)}}"
    println("{\"productionAdapter\":true,\"truePinnedNative\":true,\"cases\":[${logs.joinToString()}],\"extra\":[${extra.joinToString()}],\"failures\":${failures.joinToString(prefix="[",postfix="]") { json(it) }}}")
    if (failures.isNotEmpty()) throw AssertionError(failures.joinToString("; "))
}

private inline fun AndroidChewingEngine.use(body: (AndroidChewingEngine) -> Unit) { try { body(this) } finally { close() } }
