package com.example.androidkeyboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import com.example.androidkeyboard.engines.core.EngineUpdate
import kotlin.math.abs

/**
 * UI projection of decoder candidate state. Ordering is decoder-owned:
 * [items] must always be the decoder page order, untouched by presentation.
 */
data class CandidateState(
    val items: List<String>,
    val canPageBackward: Boolean,
    val canPageForward: Boolean,
    val expanded: Boolean,
)

/** Pure decoder-update to presentation-projection mapping. Emits no effects. */
fun candidateStateOf(
    update: EngineUpdate,
    expanded: Boolean,
    canPageBackward: Boolean,
    canPageForward: Boolean,
): CandidateState = CandidateState(
    items = update.candidates.toList(),
    canPageBackward = canPageBackward,
    canPageForward = canPageForward,
    expanded = expanded,
)

/**
 * Renders one libchewing candidate page. Paging and ranking stay decoder-owned;
 * this view only projects [CandidateState] into a collapsed strip or an
 * expanded grid. Never touches InputConnection or libchewing: user gestures
 * are forwarded through callbacks into the service/controller bridge.
 */
class CandidateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    companion object {
        const val ROW_HEIGHT_DP = 44f
        const val MAX_EXPANDED_ROWS = 4
        private const val CHEVRON_WIDTH_DP = 48f
        private const val CHIP_PADDING_DP = 16f
    }

    private data class Chip(
        val index: Int,
        val label: CharSequence,
        val rect: RectF,
    )

    private val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private var palette = ImePalette.from(context)
    private var state: CandidateState = CandidateState(emptyList(), false, false, false)

    var onItemClick: ((Int, String) -> Unit)? = null
    var onPrevPage: (() -> Unit)? = null
    var onNextPage: (() -> Unit)? = null
    var onToggleExpand: (() -> Unit)? = null

    private var chips: List<Chip> = emptyList()
    private var chevronRect = RectF()
    private var scrollXPx = 0f
    private var scrollYPx = 0f
    private var maxScrollXPx = 0f
    private var maxScrollYPx = 0f

    private var touchDownX = 0f
    private var touchDownY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var scrolling = false
    private var pressedChip = -1
    private var pressedChevron = false
    private var velocity: VelocityTracker? = null
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val minFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity

    init {
        isClickable = true
        refreshAppearance()
    }

    fun refreshAppearance() {
        palette = ImePalette.from(context)
        textPaint.textSize = 20f * resources.displayMetrics.scaledDensity
        rebuildLayout()
    }

    fun setCandidateState(next: CandidateState) {
        if (next == state) return
        state = next
        scrollXPx = 0f
        scrollYPx = 0f
        pressedChip = -1
        pressedChevron = false
        contentDescription = if (next.items.isEmpty()) {
            null
        } else {
            "候選字共 ${next.items.size} 個，${if (next.expanded) "已展開" else "已收合"}"
        }
        rebuildLayout()
    }

    /** Row count of the current content; the service sizes this view from it. */
    fun contentRowCount(): Int {
        if (state.items.isEmpty()) return 0
        return if (state.expanded) expandedRows() else 1
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != oldw || h != oldh) rebuildLayout()
    }

    private fun rowHeightPx(): Float = ROW_HEIGHT_DP * resources.displayMetrics.density

    private fun expandedRows(): Int {
        if (state.items.isEmpty() || width <= 0) return 0
        val chipPad = CHIP_PADDING_DP * resources.displayMetrics.density
        val chevronW = CHEVRON_WIDTH_DP * resources.displayMetrics.density
        var rows = 1
        var x = 0f
        val cells = state.items.size + 1
        for (i in 0 until cells) {
            val cellW = if (i < state.items.size) {
                (textPaint.measureText(state.items[i]) + chipPad * 2).coerceAtMost(width.toFloat())
            } else {
                chevronW
            }
            if (x > 0f && x + cellW > width) {
                rows++
                x = 0f
            }
            x += cellW
        }
        return rows
    }

    private fun rebuildLayout() {
        chips = emptyList()
        chevronRect = RectF()
        maxScrollXPx = 0f
        maxScrollYPx = 0f
        if (state.items.isEmpty() || width <= 0) {
            invalidate()
            return
        }
        val density = resources.displayMetrics.density
        val rowH = rowHeightPx()
        val chipPad = CHIP_PADDING_DP * density
        if (state.expanded) {
            rebuildExpanded(rowH, chipPad)
        } else {
            rebuildCollapsed(rowH, chipPad, CHEVRON_WIDTH_DP * density)
        }
        scrollXPx = scrollXPx.coerceIn(0f, maxScrollXPx)
        scrollYPx = scrollYPx.coerceIn(0f, maxScrollYPx)
        invalidate()
    }

    private fun ellipsized(text: String, maxWidthPx: Float): CharSequence {
        if (textPaint.measureText(text) <= maxWidthPx) return text
        return TextUtils.ellipsize(text, textPaint, maxWidthPx, TextUtils.TruncateAt.END)
    }

    private fun rebuildCollapsed(rowH: Float, chipPad: Float, chevronW: Float) {
        val stripW = (width - chevronW).coerceAtLeast(chevronW)
        val built = ArrayList<Chip>(state.items.size)
        var x = 0f
        state.items.forEachIndexed { index, text ->
            val cellW = (textPaint.measureText(text) + chipPad * 2)
                .coerceAtLeast(chevronW)
                .coerceAtMost(stripW.coerceAtLeast(1f))
            built.add(Chip(index, ellipsized(text, cellW - chipPad), RectF(x, 0f, x + cellW, rowH)))
            x += cellW
        }
        chips = built
        chevronRect = RectF(width - chevronW, 0f, width.toFloat(), rowH)
        maxScrollXPx = (x - stripW).coerceAtLeast(0f)
    }

    private fun rebuildExpanded(rowH: Float, chipPad: Float) {
        val chevronW = CHEVRON_WIDTH_DP * resources.displayMetrics.density
        val built = ArrayList<Chip>(state.items.size)
        var x = 0f
        var y = 0f
        state.items.forEachIndexed { index, text ->
            val cellW = (textPaint.measureText(text) + chipPad * 2)
                .coerceAtLeast(chevronW)
                .coerceAtMost(width.toFloat())
            if (x > 0f && x + cellW > width) {
                x = 0f
                y += rowH
            }
            built.add(Chip(index, ellipsized(text, cellW - chipPad), RectF(x, y, x + cellW, y + rowH)))
            x += cellW
        }
        val chevronCellW = chevronW.coerceAtMost(width.toFloat())
        var cx = x
        var cy = y
        if (cx > 0f && cx + chevronCellW > width) {
            cx = 0f
            cy += rowH
        }
        chevronRect = RectF(cx, cy, cx + chevronCellW, cy + rowH)
        chips = built
        val rows = (cy / rowH).toInt() + 1
        maxScrollYPx = (rows * rowH - MAX_EXPANDED_ROWS * rowH).coerceAtLeast(0f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(palette.surface)
        if (chips.isEmpty() || width <= 0) return

        val dx = if (state.expanded) 0f else -scrollXPx
        val dy = if (state.expanded) -scrollYPx else 0f
        canvas.save()
        canvas.translate(dx, dy)
        for (chip in chips) {
            textPaint.color = if (chip.index == pressedChip) palette.accent else palette.text
            canvas.drawText(
                chip.label.toString(),
                chip.rect.centerX(),
                chip.rect.centerY() - (textPaint.ascent() + textPaint.descent()) / 2f,
                textPaint,
            )
        }
        canvas.restore()
        val glyph = if (state.expanded) "˅" else "˄"
        val chevronDraw = RectF(
            chevronRect.left,
            chevronRect.top + dy,
            chevronRect.right,
            chevronRect.bottom + dy,
        )
        textPaint.color = if (pressedChevron) palette.accent else palette.text
        canvas.drawText(
            glyph,
            chevronDraw.centerX(),
            chevronDraw.centerY() - (textPaint.ascent() + textPaint.descent()) / 2f,
            textPaint,
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (state.items.isEmpty() || width <= 0) return false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDownX = event.x
                touchDownY = event.y
                lastX = event.x
                lastY = event.y
                scrolling = false
                velocity = (velocity ?: VelocityTracker.obtain()).also { it.clear(); it.addMovement(event) }
                val hit = hitAt(event.x, event.y)
                pressedChip = hit.first
                pressedChevron = hit.second
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                velocity?.addMovement(event)
                if (!scrolling &&
                    (abs(event.x - touchDownX) > touchSlop || abs(event.y - touchDownY) > touchSlop)
                ) {
                    scrolling = true
                    pressedChip = -1
                    pressedChevron = false
                }
                if (scrolling) {
                    if (state.expanded) {
                        scrollYPx = (scrollYPx - (event.y - lastY)).coerceIn(0f, maxScrollYPx)
                    } else {
                        scrollXPx = (scrollXPx - (event.x - lastX)).coerceIn(0f, maxScrollXPx)
                    }
                    lastX = event.x
                    lastY = event.y
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                velocity?.addMovement(event)
                velocity?.computeCurrentVelocity(1000)
                val vx = velocity?.xVelocity ?: 0f
                val vy = velocity?.yVelocity ?: 0f
                if (scrolling && abs(vx) > minFlingVelocity && abs(vx) > abs(vy) * 1.5f) {
                    if (vx > 0) onPrevPage?.invoke() else onNextPage?.invoke()
                } else if (!scrolling) {
                    val hit = hitAt(event.x, event.y)
                    if (hit.first >= 0 && hit.first == pressedChip) {
                        performClick()
                        onItemClick?.invoke(hit.first, state.items[hit.first])
                    } else if (hit.second && pressedChevron) {
                        performClick()
                        onToggleExpand?.invoke()
                    }
                }
                pressedChip = -1
                pressedChevron = false
                velocity?.recycle()
                velocity = null
                invalidate()
            }

            MotionEvent.ACTION_CANCEL -> {
                pressedChip = -1
                pressedChevron = false
                velocity?.recycle()
                velocity = null
                invalidate()
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun hitAt(x: Float, y: Float): Pair<Int, Boolean> {
        if (chevronRect.contains(x, y + if (state.expanded) scrollYPx else 0f)) return -1 to true
        val lx = if (state.expanded) x else x + scrollXPx
        val ly = if (state.expanded) y + scrollYPx else y
        for (chip in chips) {
            if (chip.rect.contains(lx, ly)) return chip.index to false
        }
        return -1 to false
    }
}
