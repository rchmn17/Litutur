package com.example.litutur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TactileBox(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    shadowColor: Color,
    cornerRadius: Dp = 16.dp,
    shadowDepth: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(shadowColor)
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
            .padding(bottom = shadowDepth)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(cornerRadius))
                .background(backgroundColor)
        ) {
            content()
        }
    }
}
