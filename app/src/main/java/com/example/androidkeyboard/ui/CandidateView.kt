package com.example.androidkeyboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CandidateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }
    private var candidates: List<String> = emptyList()
    private var currentPage = 0
    private val pageSize = 5
    var onItemClick: ((String) -> Unit)? = null

    fun setCandidates(items: List<String>) {
        this.candidates = items
        currentPage = 0
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (candidates.isEmpty()) return
        val pageStart = currentPage * pageSize
        val pageEnd = minOf(pageStart + pageSize, candidates.size)
        val page = candidates.subList(pageStart, pageEnd)
        val slotWidth = width.toFloat() / pageSize
        for (i in page.indices) {
            textPaint.color = 0xFF212121.toInt()
            canvas.drawText(page[i], slotWidth * (i + 0.5f), height / 2f + textPaint.textSize / 3f, textPaint)
        }
    }

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        when (event.actionMasked) {
            android.view.MotionEvent.ACTION_DOWN -> {
                val slotWidth = width.toFloat() / pageSize
                val clicked = (event.x / slotWidth).toInt().coerceIn(0, pageSize - 1)
                val pageStart = currentPage * pageSize
                if (pageStart + clicked < candidates.size) {
                    onItemClick?.invoke(candidates[pageStart + clicked])
                }
            }
        }
        return true
    }
}

