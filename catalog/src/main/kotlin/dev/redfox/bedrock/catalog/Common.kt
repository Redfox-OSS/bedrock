package dev.redfox.bedrock.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.redfox.bedrock.components.BrPage
import dev.redfox.bedrock.components.SectionHeader
import dev.redfox.bedrock.motion.revealOnEnter
import dev.redfox.bedrock.navigation.brNavClearance
import dev.redfox.bedrock.theme.BedrockTheme
import dev.redfox.bedrock.theme.BrSpacing

/** A root tab: a [BrPage] that leaves room for the nav bar. */
@Composable
internal fun TabPage(title: String, subtitle: String? = null, content: LazyListScope.() -> Unit) {
    BrPage(title = title, subtitle = subtitle, bottomPadding = brNavClearance(), content = content)
}

/** A pushed page with a back button. */
@Composable
internal fun DetailPage(title: String, subtitle: String? = null, content: LazyListScope.() -> Unit) {
    val navigator = LocalNavigator.current
    BrPage(title = title, subtitle = subtitle, onBack = { navigator.back() }, content = content)
}

/**
 * A titled block of a catalog page, padded to the gutter, entering in turn with its siblings.
 * [note] is a short line saying what the demo shows.
 */
internal fun LazyListScope.demo(
    title: String,
    index: Int,
    note: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    item(key = title) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = BrSpacing.Gutter)
                .padding(bottom = BrSpacing.Section - BrSpacing.S)
                .revealOnEnter(index),
        ) {
            SectionHeader(title)
            if (note != null) {
                Text(
                    note,
                    style = BedrockTheme.typography.caption,
                    color = BedrockTheme.colors.textTertiary,
                    modifier = Modifier.padding(bottom = BrSpacing.M),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(BrSpacing.M), content = content)
        }
    }
}
