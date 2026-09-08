package com.example.androidkeyboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
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
    private val pageSize: Int get() = 5
    var onItemClick: ((String) -> Unit)? = null

    /** T17: 左右滑動切換候選頁 */
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float): Boolean {
            if (e1 == null) return false
            val dx = e2.x - e1.x
            if (kotlin.math.abs(dx) > height && kotlin.math.abs(dx) > kotlin.math.abs(e2.y - e1.y)) {
                if (dx > 0 && currentPage > 0) {
                    currentPage--
                    invalidate()
                    return true
                } else if (dx < 0 && (currentPage + 1) * pageSize < candidates.size) {
                    currentPage++
                    invalidate()
                    return true
                }
            }
            return false
        }
    })

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
        /** T17: 多頁時顯示頁碼指示器 */
        if (getPageCount() > 1) {
            textPaint.color = 0xFF9E9E9E.toInt()
            textPaint.textSize = 12f
            val indicator = (currentPage + 1).toString() + \"/\" + getPageCount().toString()
            canvas.drawText(indicator, width - 30f, height / 2f + textPaint.textSize / 3f, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val slotW = width.toFloat() / pageSize
                val clicked = (event.x / slotW).toInt().coerceIn(0, pageSize - 1)
                val pageStart = currentPage * pageSize
                if (pageStart + clicked < candidates.size) {
                    onItemClick?.invoke(candidates[pageStart + clicked])
                }
            }
        }
        gestureDetector.onTouchEvent(event)
        return true
    }
}
