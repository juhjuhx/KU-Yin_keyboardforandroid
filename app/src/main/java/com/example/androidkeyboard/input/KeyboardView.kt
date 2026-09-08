package com.example.androidkeyboard.input

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.Surface

class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    data class KeySlot(val key: KeyDef, val rect: RectF)

    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }
    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val keySlots = mutableListOf<KeySlot>()
    private var rows: List<KeyboardRow> = emptyList()
    private var keyHeightDp = 60f
    private var hGapDp = 3f
    private var vGapDp = 6f
    private var pressedKeyIndex = -1
    private var hapticEnabled = true
    private var proximityTolerance = 0.15f
    var onKeyPress: ((String) -> Unit)? = null

    init {
        val density = context.resources.displayMetrics.density
        keyPaint.textSize = 28f * density
    }

    /** T16: 依螢幕旋轉自動調整鍵高，並重建 KeySlot 緩存 */
    fun refreshLayout() {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val rotation = wm?.defaultDisplay?.rotation ?: 0
        keyHeightDp = if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) 48f else 60f
        rebuildKeySlots()
    }

    fun setLayout(rows: List<KeyboardRow>) {
        this.rows = rows
        rebuildKeySlots()
    }

    fun setHaptic(enabled: Boolean) { hapticEnabled = enabled }
    fun setProximityTolerance(pct: Float) { proximityTolerance = pct.coerceIn(0f, 0.5f) }

    private fun rebuildKeySlots() {
        keySlots.clear()
        if (rows.isEmpty()) { invalidate(); return }
        val w = width.toFloat()
        val density = resources.displayMetrics.density
        val keyH = keyHeightDp * density
        val hGap = hGapDp * density
        val vGap = vGapDp * density
        var y = 0f
        for (row in rows) {
            var x = 0f
            val rowTotalPct = row.keys.sumOf { it.widthPct.toDouble() }.toFloat()
            val pctToPx = w / rowTotalPct
            for (key in row.keys) {
                val kW = key.widthPct * pctToPx - hGap
                val rect = RectF(x, y, x + kW, y + keyH - vGap)
                keySlots.add(KeySlot(key, rect))
                x += key.widthPct * pctToPx
            }
            y += keyH
        }
        invalidate()
    }

    private fun vibrate() {
        if (!hapticEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
        } else {
            (context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator)?.vibrate(10)
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val width = MeasureSpec.getSize(widthSpec)
        val keyHeight = keyHeightDp * resources.displayMetrics.density
        val totalHeight = rows.sumOf { _ -> keyHeight.toInt() } + (rows.size * vGapDp * resources.displayMetrics.density).toInt()
        setMeasuredDimension(width, maxOf(totalHeight, keyHeight.toInt()))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (keySlots.isEmpty()) return
        for ((key, rect) in keySlots) {
            val isPressed = keySlots.indexOfLast { it.rect == rect } == pressedKeyIndex
            keyBgPaint.color = if (isPressed) 0xFFBDBDBD.toInt() else 0xFFEEEEEE.toInt()
            canvas.drawRect(rect, keyBgPaint)
            keyPaint.color = if (isPressed) 0xFF424242.toInt() else 0xFF212121.toInt()
            canvas.drawText(key.label, rect.centerX(), rect.centerY() + keyPaint.textSize / 3f, keyPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                pressedKeyIndex = getKeyPressedIndex(event.x, event.y)
                invalidate()
                if (pressedKeyIndex >= 0) {
                    vibrate()
                    onKeyPress?.invoke(getKeysAt(pressedKeyIndex))
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                pressedKeyIndex = -1
                invalidate()
            }
        }
        return true
    }

    /** T16: 先精確命中，未命中則探 proximityTolerance 範圍內最近鍵 */
    private fun getKeyPressedIndex(x: Float, y: Float): Int {
        for (i in keySlots.indices) {
            if (keySlots[i].rect.contains(x, y)) return i
        }
        val tol = proximityTolerance * keyHeightDp * resources.displayMetrics.density
        var nearestIdx = -1
        var nearestDist = Float.MAX_VALUE
        for (i in keySlots.indices) {
            val d = pointToRectDist(x, y, keySlots[i].rect)
            if (d < nearestDist && d <= tol) { nearestDist = d; nearestIdx = i }
        }
        return nearestIdx
    }

    private fun pointToRectDist(px: Float, py: Float, r: RectF): Float {
        val dx = maxOf(0f, r.left - px, px - r.right)
        val dy = maxOf(0f, r.top - py, py - r.bottom)
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    private fun getKeysAt(index: Int): String {
        return if (index in keySlots.indices) keySlots[index].key.label else ""
    }
}
