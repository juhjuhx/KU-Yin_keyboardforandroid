package com.example.androidkeyboard.input

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class SymbolPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 18f
        textAlign = Paint.Align.CENTER
    }
    private var symbols: List<String> = emptyList()
    private var selectedIndex = -1
    var onSymbolSelect: ((String) -> Unit)? = null
    var onClose: (() -> Unit)? = null
    private var startX = 0f

    fun setSymbols(items: List<String>, anchorX: Float) {
        this.symbols = items
        this.startX = anchorX
        selectedIndex = -1
        visibility = VISIBLE
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (symbols.isEmpty()) return
        val slotW = width.toFloat() / symbols.size
        for (i in symbols.indices) {
            textPaint.color = if (i == selectedIndex) 0xFF38BDF8.toInt() else 0xFF212121.toInt()
            canvas.drawText(symbols[i], slotW * (i + 0.5f), height / 2f + textPaint.textSize / 3f, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val slotW = width.toFloat() / symbols.size
                selectedIndex = (event.x / slotW).toInt().coerceIn(0, symbols.size - 1)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                if (selectedIndex >= 0) onSymbolSelect?.invoke(symbols[selectedIndex])
                selectedIndex = -1
                visibility = GONE
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                selectedIndex = -1
                visibility = GONE
                invalidate()
                onClose?.invoke()
            }
        }
        return true
    }
}
