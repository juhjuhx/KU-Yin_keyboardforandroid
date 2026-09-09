package com.example.androidkeyboard.input

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import android.view.WindowManager

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
    var onKeyPress: ((KeyDef) -> Unit)? = null

    init {
        keyPaint.textSize = 28f * context.resources.displayMetrics.density
    }

    fun refreshLayout() {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val rotation = wm?.defaultDisplay?.rotation ?: 0
        keyHeightDp = if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            48f
        } else {
            60f
        }
        requestLayout()
        rebuildKeySlots()
    }

    fun setLayout(rows: List<KeyboardRow>) {
        this.rows = rows
        requestLayout()
        rebuildKeySlots()
    }

    fun setHaptic(enabled: Boolean) {
        hapticEnabled = enabled
    }

    fun setProximityTolerance(pct: Float) {
        proximityTolerance = pct.coerceIn(0f, 0.5f)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != oldw || h != oldh) rebuildKeySlots()
    }

    private fun rebuildKeySlots() {
        keySlots.clear()
        if (rows.isEmpty() || width <= 0) {
            invalidate()
            return
        }

        val w = width.toFloat()
        val density = resources.displayMetrics.density
        val keyH = keyHeightDp * density
        val hGap = hGapDp * density
        val vGap = vGapDp * density
        var y = 0f

        for (row in rows) {
            val rowTotalPct = row.keys.sumOf { it.widthPct.toDouble() }.toFloat()
            if (rowTotalPct <= 0f) continue

            var x = 0f
            val pctToPx = w / rowTotalPct
            for (key in row.keys) {
                val cellWidth = key.widthPct * pctToPx
                val keyWidth = (cellWidth - hGap).coerceAtLeast(1f)
                val keyHeight = (keyH - vGap).coerceAtLeast(1f)
                keySlots.add(KeySlot(key, RectF(x, y, x + keyWidth, y + keyHeight)))
                x += cellWidth
            }
            y += keyH
        }
        invalidate()
    }

    private fun vibrate() {
        if (!hapticEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING,
            )
        } else {
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator)?.vibrate(10)
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val measuredWidth = MeasureSpec.getSize(widthSpec)
        val keyHeight = keyHeightDp * resources.displayMetrics.density
        val totalHeight = rows.size * keyHeight.toInt()
        setMeasuredDimension(measuredWidth, maxOf(totalHeight, keyHeight.toInt()))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for ((index, slot) in keySlots.withIndex()) {
            val isPressed = index == pressedKeyIndex
            keyBgPaint.color = if (isPressed) 0xFFBDBDBD.toInt() else 0xFFEEEEEE.toInt()
            canvas.drawRect(slot.rect, keyBgPaint)
            keyPaint.color = if (isPressed) 0xFF424242.toInt() else 0xFF212121.toInt()
            canvas.drawText(
                slot.key.label,
                slot.rect.centerX(),
                slot.rect.centerY() + keyPaint.textSize / 3f,
                keyPaint,
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                pressedKeyIndex = getKeyPressedIndex(event.x, event.y)
                invalidate()
                if (pressedKeyIndex >= 0) {
                    vibrate()
                    onKeyPress?.invoke(keySlots[pressedKeyIndex].key)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                pressedKeyIndex = -1
                invalidate()
            }
        }
        return true
    }

    private fun getKeyPressedIndex(x: Float, y: Float): Int {
        for (i in keySlots.indices) {
            if (keySlots[i].rect.contains(x, y)) return i
        }

        val tol = proximityTolerance * keyHeightDp * resources.displayMetrics.density
        var nearestIdx = -1
        var nearestDist = Float.MAX_VALUE
        for (i in keySlots.indices) {
            val distance = pointToRectDist(x, y, keySlots[i].rect)
            if (distance < nearestDist && distance <= tol) {
                nearestDist = distance
                nearestIdx = i
            }
        }
        return nearestIdx
    }

    private fun pointToRectDist(px: Float, py: Float, rect: RectF): Float {
        val dx = maxOf(0f, rect.left - px, px - rect.right)
        val dy = maxOf(0f, rect.top - py, py - rect.bottom)
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
}
