package dev.redfox.bedrock.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.redfox.bedrock.R
import dev.redfox.bedrock.effects.pressable
import dev.redfox.bedrock.theme.BedrockTheme
import dev.redfox.bedrock.theme.BrShapes
import dev.redfox.bedrock.theme.BrSpacing

/**
 * A small floating menu anchored to whatever it's placed beside (put both in a `Box`): the
 * actions behind a "more" button. A raised surface with a firm hairline and a soft neutral shadow;
 * it opens and closes with the platform's quiet scale-and-fade.
 */
@Composable
public fun BrMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 6.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = BedrockTheme.colors
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.widthIn(min = 200.dp).padding(vertical = 2.dp),
        offset = offset,
        shape = BrShapes.Medium,
        containerColor = colors.surfaceRaised,
        tonalElevation = 0.dp,
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, colors.lineStrong),
        content = content,
    )
}

/**
 * One action in a [BrMenu]. [selected] marks the current choice with a check; [destructive] turns
 * the label critical (delete, remove).
 */
@Composable
public fun BrMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    selected: Boolean = false,
    destructive: Boolean = false,
    enabled: Boolean = true,
) {
    val colors = BedrockTheme.colors
    val tint = when {
        !enabled -> colors.textDisabled
        destructive -> colors.critical
        selected -> colors.accentSoft
        else -> colors.textPrimary
    }
    Row(
        modifier
            .widthIn(min = 200.dp)
            .pressable(enabled = enabled, pressedScale = 0.98f, onClick = onClick)
            .semantics {
                role = Role.Button
                this.selected = selected
            }
            .padding(horizontal = BrSpacing.L, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(BrSpacing.M))
        }
        Text(text, style = BedrockTheme.typography.bodyLarge, color = tint, modifier = Modifier.weight(1f))
        if (selected) {
            Spacer(Modifier.width(BrSpacing.M))
            Icon(painterResource(R.drawable.br_ic_check), contentDescription = null, tint = colors.accentSoft, modifier = Modifier.size(18.dp))
        }
    }
}
