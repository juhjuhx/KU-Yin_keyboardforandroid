package com.example.androidkeyboard.input

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.androidkeyboard.ui.ImePalette

class SymbolPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private var palette = ImePalette.from(context)
    private var symbols: List<String> = emptyList()
    private var selectedIndex = -1

    var onSymbolSelect: ((String) -> Unit)? = null
    var onClose: (() -> Unit)? = null

    init {
        isClickable = true
        refreshAppearance()
    }

    fun refreshAppearance() {
        palette = ImePalette.from(context)
        textPaint.textSize = 18f * resources.displayMetrics.scaledDensity
        invalidate()
    }

    fun setSymbols(items: List<String>, anchorX: Float) {
        @Suppress("UNUSED_VARIABLE")
        val anchor = anchorX
        symbols = items.toList()
        selectedIndex = -1
        visibility = if (symbols.isEmpty()) GONE else VISIBLE
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(palette.surface)
        if (symbols.isEmpty() || width <= 0) return

        val slotW = width.toFloat() / symbols.size
        for (i in symbols.indices) {
            textPaint.color = if (i == selectedIndex) palette.accent else palette.text
            canvas.drawText(
                symbols[i],
                slotW * (i + 0.5f),
                height / 2f - (textPaint.ascent() + textPaint.descent()) / 2f,
                textPaint,
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (symbols.isEmpty() || width <= 0) return false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                selectedIndex = symbolIndexAt(event.x)
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                val index = symbolIndexAt(event.x)
                if (index >= 0 && index == selectedIndex) {
                    performClick()
                    onSymbolSelect?.invoke(symbols[index])
                }
                closePicker()
            }

            MotionEvent.ACTION_CANCEL -> {
                closePicker()
                onClose?.invoke()
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun symbolIndexAt(x: Float): Int {
        if (symbols.isEmpty() || width <= 0 || x < 0f || x >= width) return -1
        val slotW = width.toFloat() / symbols.size
        return (x / slotW).toInt().coerceIn(symbols.indices)
    }

    private fun closePicker() {
        selectedIndex = -1
        visibility = GONE
        invalidate()
    }
}
