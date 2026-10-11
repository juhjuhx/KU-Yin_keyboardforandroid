package com.example.ime.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ime.session.CandidatePageSnapshot

/** Presentation only. Indices refer to the immutable page supplied by its session. */
@Composable
internal fun CandidateStrip(
    candidates: List<String>,
    page: CandidatePageSnapshot?,
    fontSize: Int,
    theme: KeyboardThemeColors,
    showExpand: Boolean,
    onSelect: (Int) -> Unit,
    onExpand: () -> Unit,
) {
    val scroll = rememberLazyListState()
    val stripHeight = with(LocalDensity.current) { (fontSize.sp.toDp() * 1.5f + 20.dp).coerceIn(56.dp, 96.dp) }
    LaunchedEffect(page?.snapshotGeneration, candidates) { scroll.scrollToItem(0) }
    Row(Modifier.fillMaxWidth().height(stripHeight), verticalAlignment = Alignment.CenterVertically) {
        LazyRow(
            state = scroll,
            modifier = Modifier.weight(1f).fillMaxHeight().testTag("candidate_strip"),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            itemsIndexed(candidates, key = { index, _ ->
                "${page?.snapshotGeneration}:${page?.ref(index)?.selectionIdentity ?: index}"
            }) { index, label ->
                CandidateChip(label, fontSize, theme, Modifier, maxLines = 1, onClick = { onSelect(index) })
            }
        }
        if (showExpand) IconButton(onClick = onExpand, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.ExpandMore, "展開候選", tint = theme.keyTextSub)
        }
    }
}

@Composable
internal fun ExpandedCandidateGrid(
    page: CandidatePageSnapshot,
    fontSize: Int,
    theme: KeyboardThemeColors,
    onSelect: (Int) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    // Shrinks in landscape/multi-window while preserving a bounded scrolling region.
    val height = (LocalConfiguration.current.screenHeightDp * 0.45f).dp.coerceIn(96.dp, 248.dp)
    Column(Modifier.fillMaxWidth().height(height).testTag("candidate_browser")) {
        key(page.snapshotGeneration) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(96.dp),
                modifier = Modifier.weight(1f).fillMaxWidth().testTag("candidate_grid"),
                contentPadding = PaddingValues(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                gridItemsIndexed(page.candidates, key = { _, item -> item.selectionIdentity }) { index, item ->
                    CandidateChip(item.label, fontSize, theme, Modifier.fillMaxWidth(), onClick = { onSelect(index) })
                }
            }
        }
        if (page.candidates.isEmpty()) Text("此頁沒有可顯示候選", color = theme.keyTextSub)
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevious, enabled = page.hasPrevious, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.ChevronLeft, "上一頁候選", tint = theme.keyTextSub)
            }
            val label = if (page.pageIndex == null) "候選頁" else {
                val current = page.pageIndex + 1
                page.totalPages?.let { "第 $current / $it 頁" } ?: "第 $current 頁"
            }
            Text(label, color = theme.keyTextPrimary, modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1)
            IconButton(onClick = onNext, enabled = page.hasNext, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.ChevronRight, "下一頁候選", tint = theme.keyTextSub)
            }
        }
    }
}

@Composable
private fun CandidateChip(
    label: String,
    fontSize: Int,
    theme: KeyboardThemeColors,
    modifier: Modifier,
    maxLines: Int = 2,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clickable(role = Role.Button, onClickLabel = "選取 $label", onClick = onClick),
        color = theme.keyBgNormal,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, theme.keyBgAccent.copy(alpha = 0.4f)),
    ) {
        Box(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
            // Semantics retain the full string even if the visual cell wraps/truncates.
            Text(label, color = theme.keyTextPrimary, fontSize = fontSize.sp,
                fontWeight = FontWeight.Medium, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
        }
    }
}
