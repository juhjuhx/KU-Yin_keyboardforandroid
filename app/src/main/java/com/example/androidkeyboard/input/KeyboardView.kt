package com.example.androidkeyboard.input

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import com.example.androidkeyboard.ui.ImePalette

class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    data class KeySlot(val key: KeyDef, val rect: RectF)

    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
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
    private var palette = ImePalette.from(context)

    var onKeyPress: ((KeyDef) -> Unit)? = null

    init {
        isClickable = true
        refreshAppearance()
    }

    fun refreshLayout() {
        keyHeightDp = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            48f
        } else {
            60f
        }
        refreshAppearance()
        requestLayout()
        rebuildKeySlots()
    }

    fun refreshAppearance() {
        palette = ImePalette.from(context)
        keyPaint.textSize = 28f * resources.displayMetrics.scaledDensity
        invalidate()
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
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val measuredWidth = MeasureSpec.getSize(widthSpec)
        val keyHeight = keyHeightDp * resources.displayMetrics.density
        val totalHeight = rows.size * keyHeight.toInt()
        setMeasuredDimension(measuredWidth, maxOf(totalHeight, keyHeight.toInt()))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(palette.surface)
        for ((index, slot) in keySlots.withIndex()) {
            val isPressed = index == pressedKeyIndex
            keyBgPaint.color = if (isPressed) palette.keyPressed else palette.key
            canvas.drawRect(slot.rect, keyBgPaint)
            keyPaint.color = if (isPressed) palette.textPressed else palette.text
            canvas.drawText(
                slot.key.label,
                slot.rect.centerX(),
                slot.rect.centerY() - (keyPaint.ascent() + keyPaint.descent()) / 2f,
                keyPaint,
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                pressedKeyIndex = getKeyPressedIndex(event.x, event.y)
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                val nextIndex = getKeyPressedIndex(event.x, event.y)
                if (nextIndex != pressedKeyIndex) {
                    pressedKeyIndex = nextIndex
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                val releasedIndex = getKeyPressedIndex(event.x, event.y)
                val commitIndex = if (releasedIndex >= 0 && releasedIndex == pressedKeyIndex) {
                    releasedIndex
                } else {
                    -1
                }
                pressedKeyIndex = -1
                invalidate()

                if (commitIndex >= 0) {
                    performClick()
                    vibrate()
                    onKeyPress?.invoke(keySlots[commitIndex].key)
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                pressedKeyIndex = -1
                invalidate()
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
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
