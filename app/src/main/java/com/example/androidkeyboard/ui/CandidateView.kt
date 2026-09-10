package com.example.androidkeyboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/** Displays one libchewing candidate page. Paging itself stays decoder-owned. */
class CandidateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private var palette = ImePalette.from(context)
    private var candidates: List<String> = emptyList()

    var onItemClick: ((Int, String) -> Unit)? = null
    var onPrevPage: (() -> Unit)? = null
    var onNextPage: (() -> Unit)? = null

    private var touchDownX = 0f
    private var touchDownY = 0f
    private var pressedIndex = -1

    init {
        isClickable = true
        refreshAppearance()
    }

    fun refreshAppearance() {
        palette = ImePalette.from(context)
        textPaint.textSize = 20f * resources.displayMetrics.scaledDensity
        invalidate()
    }

    fun setCandidates(items: List<String>) {
        candidates = items.toList()
        pressedIndex = -1
        invalidate()
    }

    // Kept for source compatibility with the earlier view contract. Native paging
    // is owned by AndroidChewingEngine, so the rendered list is always one page.
    fun getCurrentPage(): Int = 0
    fun getPageCount(): Int = 1

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(palette.surface)
        if (candidates.isEmpty() || width <= 0) return

        val slotWidth = width.toFloat() / candidates.size
        candidates.forEachIndexed { index, candidate ->
            textPaint.color = if (index == pressedIndex) palette.accent else palette.text
            canvas.drawText(
                candidate,
                slotWidth * (index + 0.5f),
                height / 2f - (textPaint.ascent() + textPaint.descent()) / 2f,
                textPaint,
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (candidates.isEmpty() || width <= 0) return false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDownX = event.x
                touchDownY = event.y
                pressedIndex = candidateIndexAt(event.x)
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                val index = candidateIndexAt(event.x)
                if (index != pressedIndex) {
                    pressedIndex = index
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                val dx = event.x - touchDownX
                val dy = abs(event.y - touchDownY)
                val swipeThreshold = height.coerceAtLeast(1).toFloat()

                if (abs(dx) > swipeThreshold && abs(dx) > dy) {
                    if (dx > 0) onPrevPage?.invoke() else onNextPage?.invoke()
                } else {
                    val index = candidateIndexAt(event.x)
                    if (index >= 0 && index == pressedIndex) {
                        performClick()
                        onItemClick?.invoke(index, candidates[index])
                    }
                }
                pressedIndex = -1
                invalidate()
            }

            MotionEvent.ACTION_CANCEL -> {
                pressedIndex = -1
                invalidate()
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun candidateIndexAt(x: Float): Int {
        if (candidates.isEmpty() || width <= 0 || x < 0f || x >= width) return -1
        val slotWidth = width.toFloat() / candidates.size
        return (x / slotWidth).toInt().coerceIn(candidates.indices)
    }
}
