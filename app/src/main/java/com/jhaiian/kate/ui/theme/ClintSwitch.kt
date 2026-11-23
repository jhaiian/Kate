package com.jhaiian.kate.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jhaiian.kate.ui.components.KateSwitch

@Composable
fun ClintSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    KateSwitch(checked = checked, onCheckedChange = onCheckedChange, modifier = modifier, enabled = enabled)
}
