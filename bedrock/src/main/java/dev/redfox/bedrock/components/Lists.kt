package dev.redfox.bedrock.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.redfox.bedrock.R
import dev.redfox.bedrock.effects.pressable
import dev.redfox.bedrock.theme.BedrockTheme
import dev.redfox.bedrock.theme.BrShapes
import dev.redfox.bedrock.theme.BrSpacing

/**
 * A grouped list: rows stacked on one card, separated by hairlines that start where the text
 * starts (not under the icons). Give each [ListRow] inside `divider = true` except the last.
 */
@Composable
public fun ListGroup(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val colors = BedrockTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .clip(BrShapes.Large)
            .background(colors.surface)
            .border(1.dp, colors.line, BrShapes.Large),
        content = content,
    )
}

/**
 * One row of a list: an optional [leading] (usually an [IconTile] or avatar), a [title] with an
 * optional [subtitle], then a [value] in tertiary text, a [trailing] slot, and a chevron when the
 * row leads somewhere. With [divider] a hairline runs under the text column to the row's end.
 */
@Composable
public fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    value: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    chevron: Boolean = false,
    divider: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    val dividerColor = colors.divider
    // The divider starts exactly where the text column starts, whatever the leading slot holds.
    var textStart by remember { mutableFloatStateOf(0f) }
    Row(
        modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.pressable(pressedScale = 0.99f, onClick = onClick) else Modifier)
            .heightIn(min = if (subtitle != null) 64.dp else 54.dp)
            .drawBehind {
                if (divider) {
                    val y = size.height - 0.5.dp.toPx()
                    drawLine(dividerColor, Offset(textStart + BrSpacing.L.toPx(), y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                }
            }
            .padding(horizontal = BrSpacing.L, vertical = BrSpacing.M),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BrSpacing.M),
    ) {
        leading?.invoke()
        Column(Modifier.weight(1f).onPlaced { textStart = it.positionInParent().x }) {
            Text(title, style = type.label, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Spacer(Modifier.size(2.dp))
                Text(subtitle, style = type.caption, color = colors.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        if (value != null) {
            Text(value, style = type.figure, color = colors.textTertiary, maxLines = 1)
        }
        trailing?.invoke()
        if (chevron) {
            Icon(
                painterResource(R.drawable.br_ic_chevron_right),
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
