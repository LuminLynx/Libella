package com.example.foss101.ui.preview.bite

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Bundle 0 v3 — vertical bite feed.
 *
 * The user lands on the first bite of a concept (e.g. Tokenization). They swipe
 * up to advance, swipe down to go back, or tap the chevron buttons. A stories-style
 * progress bar at the top shows where they are in the concept.
 *
 * Each bite is one focused interaction (or a final MCQ checkpoint). Reading is
 * minimal; the meat of every bite is the user *doing* something. This is the
 * mobile-native, video-feed-shaped consumption mode targeted at students /
 * younger adults — a "TikTok shape, study-app substance" experiment.
 */
@Composable
fun BiteFeedScreen(
    bites: List<Bite>,
    onClose: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { bites.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        StoriesProgress(
            count = bites.size,
            current = pagerState.currentPage,
            onClose = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )

        VerticalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            BitePage(
                bite = bites[page],
                index = page,
                total = bites.size,
                onPrev = {
                    if (page > 0) scope.launch { pagerState.animateScrollToPage(page - 1) }
                },
                onNext = {
                    if (page < bites.size - 1) scope.launch { pagerState.animateScrollToPage(page + 1) }
                }
            )
        }
    }
}

@Composable
private fun StoriesProgress(
    count: Int,
    current: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(count) { index ->
                val target = when {
                    index < current -> 1f
                    index == current -> 1f
                    else -> 0f
                }
                val progress by animateFloatAsState(
                    targetValue = target,
                    label = "story-segment-$index"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close"
            )
        }
    }
}

@Composable
private fun BitePage(
    bite: Bite,
    index: Int,
    total: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BiteHeader(bite = bite, index = index, total = total)

        // Content area: scrollable so taller interactions still fit on small screens.
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { bite.content() }
        }

        BiteNavRow(
            isFirst = index == 0,
            isLast = index == total - 1,
            onPrev = onPrev,
            onNext = onNext
        )
    }
}

@Composable
private fun BiteHeader(bite: Bite, index: Int, total: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "Bite ${index + 1} of $total · ${bite.kicker}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = bite.title,
            style = MaterialTheme.typography.headlineSmall
        )
        if (bite.hook != null) {
            Text(
                text = bite.hook,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BiteNavRow(
    isFirst: Boolean,
    isLast: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev, enabled = !isFirst) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Previous"
                )
            }
            Text(
                text = if (isLast) "Last bite — swipe down to revisit" else "Swipe up for next",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onNext, enabled = !isLast) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Next"
                )
            }
        }
    }
}

/**
 * Minimal data shape for a bite. The kicker / title / hook scaffold is small on
 * purpose — the user is meant to drop into [content] within a second or two.
 */
data class Bite(
    val kicker: String,
    val title: String,
    val hook: String? = null,
    val content: @Composable () -> Unit
)

@Composable
@Suppress("unused")
private fun NoopLaunchedEffect() {
    // Reserved for future bite entrance animations.
    LaunchedEffect(Unit) { }
}
