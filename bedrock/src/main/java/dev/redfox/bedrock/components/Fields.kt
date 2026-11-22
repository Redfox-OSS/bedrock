package dev.redfox.bedrock.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.redfox.bedrock.R
import dev.redfox.bedrock.effects.glass
import dev.redfox.bedrock.effects.pressable
import dev.redfox.bedrock.motion.BrDurations
import dev.redfox.bedrock.motion.BrMotion
import dev.redfox.bedrock.theme.BedrockTheme
import dev.redfox.bedrock.theme.BrShapes
import dev.redfox.bedrock.theme.BrSpacing

/**
 * A text field on a raised surface. Its edge warms to the accent while focused and turns to the
 * critical tone when [isError]; [supportingText] below explains either state. Single-line fields
 * are 52dp tall; multi-line ones grow from 104dp.
 */
@Composable
public fun BrTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    leadingIcon: Painter? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val edge by animateColorAsState(
        when {
            isError -> colors.critical.copy(alpha = 0.7f)
            focused -> colors.accentMain.copy(alpha = 0.6f)
            else -> colors.line
        },
        BrMotion.quick(),
        label = "fieldEdge",
    )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(BrSpacing.S)) {
        if (label != null) Text(label, style = type.labelSmall, color = colors.textSecondary)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = type.bodyLarge.copy(color = colors.textPrimary),
            cursorBrush = SolidColor(colors.accentMain),
            interactionSource = interaction,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = if (singleLine) 52.dp else 104.dp)
                        .clip(BrShapes.Medium)
                        .background(colors.surfaceRaised)
                        .border(1.dp, edge, BrShapes.Medium)
                        .padding(horizontal = BrSpacing.L, vertical = 14.dp),
                    verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(BrSpacing.M),
                ) {
                    if (leadingIcon != null) {
                        Icon(leadingIcon, contentDescription = null, tint = colors.textTertiary, modifier = Modifier.size(20.dp))
                    }
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty()) Text(placeholder, style = type.bodyLarge, color = colors.textTertiary)
                        inner()
                    }
                }
            },
        )
        if (supportingText != null) {
            Text(supportingText, style = type.caption, color = if (isError) colors.critical else colors.textTertiary)
        }
    }
}

/**
 * A frosted search pill for the top of a page. The clear button fades in once there's a query;
 * the keyboard's action key reads "Search" and calls [onSearch].
 */
@Composable
public fun BrSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    onSearch: (String) -> Unit = {},
) {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = type.bodyLarge.copy(color = colors.textPrimary),
        cursorBrush = SolidColor(colors.accentMain),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .glass(CircleShape)
                    .padding(start = 14.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(painterResource(R.drawable.br_ic_search), contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) Text(placeholder, style = type.bodyLarge, color = colors.textTertiary)
                    inner()
                }
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn(tween(BrDurations.Swap, easing = BrMotion.Calm)) + scaleIn(tween(BrDurations.Swap, easing = BrMotion.Calm), initialScale = 0.85f),
                    exit = fadeOut(tween(BrDurations.Fade)) + scaleOut(tween(BrDurations.Fade), targetScale = 0.85f),
                ) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .pressable(pressedScale = 0.9f) { onQueryChange("") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            Modifier.size(22.dp).clip(CircleShape).background(colors.surfaceHigh),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(painterResource(R.drawable.br_ic_close), contentDescription = "Clear", tint = colors.textSecondary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        },
    )
}
