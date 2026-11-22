package dev.redfox.bedrock.catalog

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** The catalog's five tabs, in bar order. */
internal enum class Tab(val label: String, @param:DrawableRes val icon: Int) {
    Today("Today", R.drawable.ic_today),
    Foundations("Foundations", R.drawable.ic_swatch),
    Components("Components", R.drawable.ic_blocks),
    Motion("Motion", R.drawable.ic_wave),
    Principles("Principles", R.drawable.ic_compass),
}

/** A place in the catalog. */
internal sealed interface Destination {
    data class Root(val tab: Tab) : Destination
    data class Components(val category: Category) : Destination
    data class PushedDemo(val depth: Int) : Destination
}

/** How the last move happened: it picks the transition and whether entrances replay. */
internal sealed interface Move {
    data class TabSwitch(val forward: Boolean) : Move
    data object Push : Move
    data object Pop : Move
}

/** The back stack: a tab's root, with pushed screens on top. */
@Stable
internal class CatalogNavigator {
    val backStack = mutableStateListOf<Destination>(Destination.Root(Tab.Today))
    var lastMove: Move by mutableStateOf(Move.Push)
        private set

    val tab: Tab get() = (backStack.first() as Destination.Root).tab
    val atRoot: Boolean get() = backStack.size == 1

    fun select(target: Tab) {
        if (target == tab) {
            while (backStack.size > 1) back()
            return
        }
        lastMove = Move.TabSwitch(forward = target.ordinal > tab.ordinal)
        backStack.clear()
        backStack.add(Destination.Root(target))
    }

    fun push(destination: Destination) {
        lastMove = Move.Push
        backStack.add(destination)
    }

    /** False when there's nowhere to go back to (leave the app). */
    fun back(): Boolean {
        if (backStack.size <= 1) return false
        lastMove = Move.Pop
        backStack.removeAt(backStack.lastIndex)
        return true
    }
}
