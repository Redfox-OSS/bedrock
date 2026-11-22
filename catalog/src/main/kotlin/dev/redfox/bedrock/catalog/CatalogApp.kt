package dev.redfox.bedrock.catalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.IntOffset
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.chrisbanes.haze.rememberHazeState
import dev.redfox.bedrock.components.BrToastHost
import dev.redfox.bedrock.components.BrToastState
import dev.redfox.bedrock.effects.LocalHazeState
import dev.redfox.bedrock.motion.BrDurations
import dev.redfox.bedrock.motion.BrMotion
import dev.redfox.bedrock.motion.BrTransitions
import dev.redfox.bedrock.motion.RevealScope
import dev.redfox.bedrock.navigation.BrNavBar
import dev.redfox.bedrock.navigation.BrNavItem
import dev.redfox.bedrock.navigation.brNavClearance
import dev.redfox.bedrock.navigation.rememberCompactNavState
import dev.redfox.bedrock.theme.BedrockAccent
import dev.redfox.bedrock.theme.BedrockNeutrals
import dev.redfox.bedrock.theme.BedrockTheme

/** Toasts for the whole catalog, shown above the nav bar. */
internal val LocalToasts = staticCompositionLocalOf { BrToastState() }

/** The navigator, for screens that push or go back. */
internal val LocalNavigator = staticCompositionLocalOf { CatalogNavigator() }

/** The theme the Foundations tab changes. */
internal class ThemeChoice(
    val accent: BedrockAccent,
    val neutrals: BedrockNeutrals,
    val onAccent: (BedrockAccent) -> Unit,
    val onNeutrals: (BedrockNeutrals) -> Unit,
)

internal val LocalThemeChoice = compositionLocalOf<ThemeChoice> { error("No theme choice") }

@Composable
internal fun CatalogApp(onExit: () -> Unit) {
    var accentIndex by rememberSaveable { mutableIntStateOf(0) }
    var neutralsIndex by rememberSaveable { mutableIntStateOf(0) }
    val accent = BedrockAccent.Presets[accentIndex]
    val neutrals = BedrockNeutrals.Presets[neutralsIndex]
    val choice = ThemeChoice(
        accent = accent,
        neutrals = neutrals,
        onAccent = { accentIndex = BedrockAccent.Presets.indexOf(it).coerceAtLeast(0) },
        onNeutrals = { neutralsIndex = BedrockNeutrals.Presets.indexOf(it).coerceAtLeast(0) },
    )
    val navigator = remember { CatalogNavigator() }
    val toasts = remember { BrToastState() }
    val hazeState = rememberHazeState()

    BedrockTheme(accent = accent, neutrals = neutrals) {
        CompositionLocalProvider(
            LocalHazeState provides hazeState,
            LocalToasts provides toasts,
            LocalNavigator provides navigator,
            LocalThemeChoice provides choice,
        ) {
            val compactNav = rememberCompactNavState(resetKey = navigator.backStack.lastOrNull())
            Box(
                Modifier
                    .fillMaxSize()
                    .background(BedrockTheme.colors.background)
                    .nestedScroll(compactNav.connection)
            ) {
                NavDisplay(
                    backStack = navigator.backStack,
                    onBack = { if (!navigator.back()) onExit() },
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                    transitionSpec = {
                        when (val move = navigator.lastMove) {
                            is Move.TabSwitch -> BrTransitions.tab(move.forward)
                            else -> BrTransitions.push()
                        }
                    },
                    popTransitionSpec = { BrTransitions.pop() },
                    predictivePopTransitionSpec = { BrTransitions.predictivePop() },
                    entryProvider = entryProvider {
                        entry<Destination.Root> { key ->
                            Reveal(navigator) {
                                when (key.tab) {
                                    Tab.Today -> TodayScreen()
                                    Tab.Foundations -> FoundationsScreen()
                                    Tab.Components -> ComponentsScreen()
                                    Tab.Motion -> MotionScreen()
                                    Tab.Principles -> PrinciplesScreen()
                                }
                            }
                        }
                        entry<Destination.Components> { key -> Reveal(navigator) { CategoryScreen(key.category) } }
                        entry<Destination.PushedDemo> { key -> Reveal(navigator) { PushedDemoScreen(key.depth) } }
                    },
                )
                AnimatedVisibility(
                    visible = navigator.atRoot,
                    enter = fadeIn(tween(BrDurations.Enter, easing = BrMotion.Calm)) +
                        slideInVertically(BrMotion.glide(IntOffset.VisibilityThreshold)) { it / 3 },
                    exit = fadeOut(tween(BrDurations.Fade)) +
                        slideOutVertically(BrMotion.settle(IntOffset.VisibilityThreshold)) { it / 3 },
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                    BrNavBar(
                        items = Tab.entries.map { BrNavItem(it.icon, it.label) },
                        selectedIndex = navigator.tab.ordinal,
                        onSelect = { navigator.select(Tab.entries[it]) },
                        compact = compactNav.compact,
                    )
                }
                BrToastHost(
                    toasts,
                    Modifier.align(Alignment.BottomCenter),
                    bottomPadding = if (navigator.atRoot) brNavClearance() else 32.dp,
                )
            }
        }
    }
}

/** Entrances play when a screen arrives, not when it's uncovered by going back. */
@Composable
private fun Reveal(navigator: CatalogNavigator, content: @Composable () -> Unit) {
    RevealScope(enabled = navigator.lastMove != Move.Pop, content = content)
}
