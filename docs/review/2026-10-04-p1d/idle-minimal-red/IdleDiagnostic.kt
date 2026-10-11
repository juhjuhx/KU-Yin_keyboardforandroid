package com.example.ime

import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

/** 暫時診斷用；不得納入最終回歸測試。 */
internal fun printIdleDiagnostic(rule: Any) {
    fun field(target: Any, name: String): Any? {
        var type: Class<*>? = target.javaClass
        while (type != null) {
            try {
                return type.getDeclaredField(name).apply { isAccessible = true }.get(target)
            } catch (_: NoSuchFieldException) { type = type.superclass }
        }
        error("Missing $name")
    }
    val environment = field(rule, "environment")!!
    val resource = field(environment, "composeIdlingResource")!!
    val flags = listOf("hadAwaitersOnMainClock", "hadSnapshotChanges", "hadRecomposerChanges",
        "hadPendingSetContent", "hadPendingMeasureLayout").associateWith { field(resource, it) }
    val message = resource.javaClass.getMethod("getDiagnosticMessageIfBusy").invoke(resource)
    val registry = field(environment, "composeRootRegistry")!!
    val roots = registry.javaClass.getMethod("getCreatedComposeRoots").invoke(registry)
    val manager = Class.forName("androidx.compose.ui.platform.GlobalSnapshotManager")
    val sent = manager.getDeclaredField("sent").apply { isAccessible = true }.get(null)
    val dispatcherType = Class.forName("androidx.compose.ui.platform.AndroidUiDispatcher")
    val companion = dispatcherType.getDeclaredField("Companion").get(null)
    val context = companion.javaClass.getMethod("getMain").invoke(companion) as CoroutineContext
    val dispatcher = context[ContinuationInterceptor]!!
    val dispatchFlags = listOf("scheduledTrampolineDispatch", "scheduledFrameDispatch",
        "toRunTrampolined", "toRunOnFrame").associateWith { field(dispatcher, it) }
    println("P1D_IDLE_DIAGNOSTIC flags=$flags message=$message roots=$roots sent=$sent dispatch=$dispatchFlags")
}
