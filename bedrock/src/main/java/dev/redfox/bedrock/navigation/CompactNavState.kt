package dev.redfox.bedrock.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Whether the [BrNavBar] should be compact, worked out from scrolling. Attach [connection] with
 * `Modifier.nestedScroll` on the screen container and pass [compact] to the bar.
 *
 * The bar only changes after the content has travelled a short distance in one direction, so a
 * slow drag, a nudge, or a finger resting on the list never makes it flicker between forms.
 */
@Stable
public class CompactNavState internal constructor(
    private val state: MutableState<Boolean>,
    private val thresholdPx: Float,
) {
    /** True while the content is being read: scrolled toward its end. */
    public val compact: State<Boolean> get() = state

    /** Distance scrolled in the current direction; negative is toward the end. */
    private var travel = 0f

    internal fun expand() {
        travel = 0f
        state.value = false
    }

    /** Observes every vertical scroll beneath it. Consumes nothing, so lists behave as before. */
    public val connection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            // Only y is read, so horizontal swipes (a row of chips) never change the bar.
            val dy = available.y
            if (dy == 0f) return Offset.Zero
            // A change of direction starts the count again.
            if ((dy < 0f) != (travel < 0f)) travel = 0f
            travel += dy
            if (travel < -thresholdPx) state.value = true
            else if (travel > thresholdPx) state.value = false
            return Offset.Zero
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            // Leftover downward delta means the list is already at its top: always show the full bar.
            if (available.y > 0f) expand()
            return Offset.Zero
        }
    }
}

/**
 * A [CompactNavState] that turns compact once content has scrolled [threshold] toward its end, and
 * expands when it scrolls back by the same distance, reaches its top, or [resetKey] (the current
 * destination) changes.
 */
@Composable
public fun rememberCompactNavState(resetKey: Any?, threshold: Dp = 24.dp): CompactNavState {
    val px = with(LocalDensity.current) { threshold.toPx() }
    val state = remember(px) { CompactNavState(mutableStateOf(false), px) }
    LaunchedEffect(resetKey) { state.expand() }
    return state
}
