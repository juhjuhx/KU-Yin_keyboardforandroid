package io.github.juhjuhx.kuyin.acceptancehost

import android.os.SystemClock
import android.view.View
import org.json.JSONArray
import org.json.JSONObject

/** TEST_ONLY: geometry/identity only; no text, focus requests or InputConnection calls. */
internal class FocusLayoutTrace(private val editor: View) {
    private val observers = mutableListOf<Pair<View, View.OnLayoutChangeListener>>()
    private val events = ArrayDeque<JSONObject>()
    private var firstLoss: List<JSONObject>? = null
    private var lastLoss: List<JSONObject>? = null

    private fun chain(): List<View> = buildList {
        var current: View? = editor
        while (current != null && size < 12) {
            add(current)
            current = current.parent as? View
        }
    }

    private fun node(v: View) = JSONObject().apply {
        put("class", v.javaClass.name)
        put("id", v.id)
        put("identity", System.identityHashCode(v))
        put("width", v.width); put("height", v.height)
        put("measured_width", v.measuredWidth); put("measured_height", v.measuredHeight)
        put("focused", v.isFocused); put("has_focus", v.hasFocus())
        put("attached", v.isAttachedToWindow); put("layout_requested", v.isLayoutRequested)
        put("focusable", v.isFocusable); put("focusable_in_touch_mode", v.isFocusableInTouchMode)
        put("window_focus", v.hasWindowFocus())
    }

    fun attach() {
        detach()
        chain().forEach { view ->
            val listener = View.OnLayoutChangeListener { v, l, t, r, b, ol, ot, or, ob ->
                val event = node(v).apply {
                    put("elapsed_ns", SystemClock.elapsedRealtimeNanos())
                    put("old_bounds", JSONArray(listOf(ol, ot, or, ob)))
                    put("new_bounds", JSONArray(listOf(l, t, r, b)))
                }
                if (events.size == 32) events.removeFirst()
                events.addLast(event)
            }
            view.addOnLayoutChangeListener(listener)
            observers.add(view to listener)
        }
    }

    fun detach() {
        observers.forEach { (view, listener) -> view.removeOnLayoutChangeListener(listener) }
        observers.clear()
    }

    fun loss() {
        val snapshot = chain().map(::node)
        if (firstLoss == null) firstLoss = snapshot
        lastLoss = snapshot
    }

    fun snapshot() = JSONObject().apply {
        put("schema", 1)
        put("clock", "GUEST_ELAPSED_REALTIME_POST_CALLBACK_NOT_HOST_TIME")
        put("observer_count", observers.size)
        put("current_ancestry", JSONArray(chain().map(::node)))
        put("first_loss_ancestry", firstLoss?.let { JSONArray(it) } ?: JSONObject.NULL)
        put("last_loss_ancestry", lastLoss?.let { JSONArray(it) } ?: JSONObject.NULL)
        put("layout_events", JSONArray(events.toList()))
        put("focus_owner", editor.rootView.findFocus()?.let(::node) ?: JSONObject.NULL)
    }
}
