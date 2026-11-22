package dev.redfox.bedrock.components

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.redfox.bedrock.R
import dev.redfox.bedrock.effects.glass
import dev.redfox.bedrock.effects.glassSource
import dev.redfox.bedrock.motion.revealOnEnter
import dev.redfox.bedrock.theme.BedrockTheme
import dev.redfox.bedrock.theme.BrSpacing

private val SideSlot = 96.dp

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
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = status)
                .height(TopBarHeight)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Equal side slots keep the small title truly centred.
            Row(Modifier.width(SideSlot), verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) BrIconButton(painterResource(R.drawable.br_ic_back), "Back", onBack, variant = IconButtonVariant.Glass)
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
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
            }
            Row(
                Modifier.width(SideSlot),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
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
    val titleSpan = with(LocalDensity.current) { 56.dp.toPx() }
    val collapse = remember(state) {
        derivedStateOf {
            if (state.firstVisibleItemIndex > 0) 1f
            else (state.firstVisibleItemScrollOffset / titleSpan).coerceIn(0f, 1f)
        }
    }
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
