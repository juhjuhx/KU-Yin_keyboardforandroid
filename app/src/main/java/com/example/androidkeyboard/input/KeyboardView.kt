package com.example.androidkeyboard.input

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }
    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val pressedBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val keyRects = mutableListOf<RectF>()
    private var rows: List<KeyboardRow> = emptyList()
    private var keyHeightDp = 60f
    private var hGapDp = 3f
    private var vGapDp = 6f
    private var pressedKeyIndex = -1
    var onKeyPress: ((String) -> Unit)? = null

    init {
        val density = context.resources.displayMetrics.density
        keyPaint.textSize = 28f * density
    }

    fun setLayout(rows: List<KeyboardRow>) {
        this.rows = rows
        keyRects.clear()
        invalidate()
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val width = MeasureSpec.getSize(widthSpec)
        val keyHeight = keyHeightDp * resources.displayMetrics.density
        val totalHeight = rows.sumOf { _ -> keyHeight.toInt() } + (rows.size * vGapDp * resources.displayMetrics.density).toInt()
        setMeasuredDimension(width, maxOf(totalHeight, keyHeight.toInt()))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (rows.isEmpty()) return
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
                keyRects.add(rect)

                val isPressed = keyRects.indexOfLast { r -> r == rect } == pressedKeyIndex
                keyBgPaint.color = if (isPressed) 0xFFBDBDBD.toInt() else 0xFFEEEEEE.toInt()
                canvas.drawRect(rect, keyBgPaint)

                keyPaint.color = if (isPressed) 0xFF424242.toInt() else 0xFF212121.toInt()
                canvas.drawText(key.label, rect.centerX(), rect.centerY() + keyPaint.textSize / 3f, keyPaint)

                x += key.widthPct * pctToPx
            }
            y += keyH
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                pressedKeyIndex = getKeyPressedIndex(event.x, event.y)
                invalidate()
                if (pressedKeyIndex >= 0) {
                    val key = getKeysAt(pressedKeyIndex)
                    onKeyPress?.invoke(key)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (pressedKeyIndex >= 0) {
                    val key = getKeysAt(pressedKeyIndex)
                    onKeyPress?.invoke("release" + key)
                }
                pressedKeyIndex = -1
                invalidate()
            }
        }
        return true
    }

    private fun getKeyPressedIndex(x: Float, y: Float): Int {
        for (i in keyRects.indices) {
            if (keyRects[i].contains(x, y)) return i
        }
        return -1
    }

    private fun getKeysAt(index: Int): String {
        var count = 0
        for (row in rows) {
            for (key in row.keys) {
                if (count == index) return key.label
                count++
            }
        }
        return "",
    }
}

