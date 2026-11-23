package dev.redfox.bedrock.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.redfox.bedrock.R
import dev.redfox.bedrock.effects.glass
import dev.redfox.bedrock.effects.glassSource
import dev.redfox.bedrock.motion.SwapText
import dev.redfox.bedrock.motion.revealOnEnter
import dev.redfox.bedrock.theme.BedrockTheme
import dev.redfox.bedrock.theme.BrSpacing

/** Height of the bar at the top of a [BrPage], below the status bar. */
public val TopBarHeight: Dp = 56.dp

/**
 * The top bar on its own, for pages that build their own scrolling. [collapse] (0 to 1) is how far
 * the page has scrolled under it: at 0 the bar is clear and the title hidden (the page shows its
 * large title); as it rises the glass fades in, a hairline appears, and the small title comes up.
 * It's read in the draw phase, so scrolling never recomposes the bar.
 */
@Composable
public fun BrTopBar(
    title: String,
    collapse: State<Float>,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    val status = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(modifier.fillMaxWidth().height(status + TopBarHeight)) {
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer { alpha = collapse.value }
                .glass(RectangleShape, rim = false)
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(1.dp)
                .graphicsLayer { alpha = collapse.value }
                .background(colors.line)
        )
        TopBarSlots(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = status)
                .height(TopBarHeight)
                .padding(horizontal = 12.dp),
            start = {
                if (onBack != null) BrIconButton(painterResource(R.drawable.br_ic_back), "Back", onBack, variant = IconButtonVariant.Glass)
            },
            title = {
                Text(
                    title,
                    style = type.label,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.graphicsLayer {
                        // The small title arrives over the last half of the collapse.
                        val t = ((collapse.value - 0.5f) * 2f).coerceIn(0f, 1f)
                        alpha = t
                        translationY = (1f - t) * 6.dp.toPx()
                    },
                )
            },
            end = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically, content = actions)
            },
        )
    }
}

/**
 * Start slot, centred title, end slot. The slots take what they need (one back button, three
 * actions); the title gets the width left between the wider of the two on both sides, so it stays
 * truly centred however the actions change.
 */
@Composable
private fun TopBarSlots(
    modifier: Modifier,
    start: @Composable () -> Unit,
    title: @Composable () -> Unit,
    end: @Composable () -> Unit,
) {
    Layout(
        contents = listOf(start, title, end),
        modifier = modifier,
    ) { (startM, titleM, endM), constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val s = startM.map { it.measure(loose) }
        val e = endM.map { it.measure(loose) }
        val side = maxOf(s.maxOfOrNull { it.width } ?: 0, e.maxOfOrNull { it.width } ?: 0)
        val gap = 8.dp.roundToPx()
        val titleWidth = (constraints.maxWidth - 2 * (side + gap)).coerceAtLeast(0)
        val t = titleM.map { it.measure(loose.copy(maxWidth = titleWidth)) }
        val h = constraints.maxHeight
        layout(constraints.maxWidth, h) {
            s.forEach { it.place(0, (h - it.height) / 2) }
            e.forEach { it.place(constraints.maxWidth - it.width, (h - it.height) / 2) }
            t.forEach { it.place((constraints.maxWidth - it.width) / 2, (h - it.height) / 2) }
        }
    }
}

/**
 * The top of the page inset a [BrTopBar] needs: status bar plus [TopBarHeight]. Pad scrolling
 * content by this so it starts below the bar.
 */
@Composable
public fun topBarClearance(): Dp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + TopBarHeight

/**
 * How far a list has scrolled under its [BrTopBar] (0 to 1, over the first [span]). Put a
 * [BrLargeTitle] as the list's first item; once it has scrolled away the bar is fully frosted.
 */
@Composable
public fun rememberTopBarCollapse(state: LazyListState, span: Dp = 56.dp): State<Float> {
    val px = with(LocalDensity.current) { span.toPx() }
    return remember(state, px) {
        derivedStateOf {
            if (state.firstVisibleItemIndex > 0) 1f
            else (state.firstVisibleItemScrollOffset / px).coerceIn(0f, 1f)
        }
    }
}

/** [rememberTopBarCollapse] for a plain `verticalScroll` column. */
@Composable
public fun rememberTopBarCollapse(state: ScrollState, span: Dp = 56.dp): State<Float> {
    val px = with(LocalDensity.current) { span.toPx() }
    return remember(state, px) { derivedStateOf { (state.value / px).coerceIn(0f, 1f) } }
}

/**
 * The large page title (and optional subtitle) that sits at the top of a page's content, below a
 * [BrTopBar]. It fades as [collapse] rises, as the bar's small title takes over.
 */
@Composable
public fun BrLargeTitle(
    title: String,
    collapse: State<Float>,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = BrSpacing.Gutter)
            .padding(top = BrSpacing.XS, bottom = BrSpacing.XL)
            .graphicsLayer { alpha = 1f - collapse.value }
    ) {
        SwapText(title, type.title, colors.textPrimary)
        if (subtitle != null) {
            Spacer(Modifier.height(BrSpacing.XS))
            Text(subtitle, style = type.body, color = colors.textSecondary)
        }
    }
}

/**
 * A full page: a large title at the top of a scrolling list, with a frosted bar that takes over
 * the title as you scroll. Put the page's content in [content] as lazy items; pad nothing at the
 * top (the page does), and set [bottomPadding] to clear a nav bar.
 */
@Composable
public fun BrPage(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    state: LazyListState = rememberLazyListState(),
    bottomPadding: Dp = BrSpacing.XXL,
    actions: @Composable RowScope.() -> Unit = {},
    content: LazyListScope.() -> Unit,
) {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    val status = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val collapse = rememberTopBarCollapse(state)
    Box(modifier.fillMaxSize()) {
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize().glassSource(),
            contentPadding = PaddingValues(top = status + TopBarHeight, bottom = bottomPadding),
        ) {
            item(key = "br-page-title") {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = BrSpacing.Gutter)
                        .padding(top = BrSpacing.XS, bottom = BrSpacing.XL)
                        .graphicsLayer {
                            // The large title fades as it slides under the bar.
                            alpha = 1f - collapse.value
                        }
                ) {
                    Text(title, style = type.title, color = colors.textPrimary, modifier = Modifier.revealOnEnter(0))
                    if (subtitle != null) {
                        Spacer(Modifier.height(BrSpacing.XS))
                        Text(subtitle, style = type.body, color = colors.textSecondary, modifier = Modifier.revealOnEnter(1))
                    }
                }
            }
            content()
        }
        BrTopBar(title = title, collapse = collapse, onBack = onBack, actions = actions)
    }
}
