package com.example.androidkeyboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

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
    private val pageSize: Int get() = 5
    var onItemClick: ((String) -> Unit)? = null
    var onPrevPage: (() -> Unit)? = null
    var onNextPage: (() -> Unit)? = null

    private var touchDownX = 0f
    private var touchDownY = 0f

    fun setCandidates(items: List<String>) {
        this.candidates = items
        currentPage = 0
        invalidate()
    }

    fun getCurrentPage(): Int = currentPage
    fun getPageCount(): Int = if (candidates.isEmpty()) 1 else ((candidates.size - 1) / pageSize) + 1

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (candidates.isEmpty()) return
        val pageStart = currentPage * pageSize
        val pageEnd = minOf(pageStart + pageSize, candidates.size)
        val page = candidates.subList(pageStart, pageEnd)
        val slotW = width.toFloat() / pageSize
        for (i in page.indices) {
            textPaint.color = 0xFF212121.toInt()
            canvas.drawText(page[i], slotW * (i + 0.5f), height / 2f + textPaint.textSize / 3f, textPaint)
        }
        if (getPageCount() > 1) {
            textPaint.color = 0xFF9E9E9E.toInt()
            textPaint.textSize = 12f
            val indicator = "${currentPage + 1}/${getPageCount()}"
            canvas.drawText(indicator, width - 30f, height / 2f + textPaint.textSize / 3f, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDownX = event.x
                touchDownY = event.y
                val slotW = width.toFloat() / pageSize
                val clicked = (event.x / slotW).toInt().coerceIn(0, pageSize - 1)
                val pageStart = currentPage * pageSize
                if (pageStart + clicked < candidates.size) {
                    onItemClick?.invoke(candidates[pageStart + clicked])
                }
            }
            MotionEvent.ACTION_UP -> {
                val dx = event.x - touchDownX
                val dy = abs(event.y - touchDownY)
                if (abs(dx) > height && abs(dx) > dy) {
                    if (dx > 0) {
                        onPrevPage?.invoke()
                    } else {
                        onNextPage?.invoke()
                    }
                }
            }
        }
        return true
    }
}
