package com.example.litutur.ui.parent

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.litutur.ui.theme.AlertDangerCoral
import com.example.litutur.ui.theme.BorderDividerColor
import com.example.litutur.ui.theme.CoralOrangePrimary
import com.example.litutur.ui.theme.SubtleHighlightOrange
import com.example.litutur.ui.theme.TextPrimarySlate
import com.example.litutur.ui.theme.TextSecondarySlate
import kotlinx.coroutines.delay

private const val PIN_LENGTH = 4
private const val DEFAULT_PIN = "1234"

/**
 * Parental Gate: keypad angka di dalam dialog (tidak bergantung pada keyboard sistem).
 * PIN otomatis diperiksa ketika 4 digit terisi.
 */
@Composable
fun ParentPinDialog(
    expectedPin: String = DEFAULT_PIN,
    title: String = "Verifikasi Orang Tua",
    message: String = "Masukkan PIN 4 digit untuk kembali ke Mode Orang Tua.",
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val correctPin = expectedPin.trim().takeIf { it.length == PIN_LENGTH && it.all(Char::isDigit) } ?: DEFAULT_PIN
    val currentOnSuccess by rememberUpdatedState(onSuccess)

    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var failedAttempts by remember { mutableIntStateOf(0) }
    var isVerified by remember { mutableStateOf(false) }
    val shake = remember { Animatable(0f) }

    // Periksa otomatis setelah digit terakhir (jeda singkat agar titik ke-4 sempat terlihat)
    LaunchedEffect(pin) {
        if (pin.length == PIN_LENGTH && !isVerified) {
            delay(150)
            if (pin == correctPin) {
                isVerified = true
                currentOnSuccess()
            } else {
                failedAttempts++
                errorMessage = "PIN salah. Coba lagi ya."
                repeat(3) {
                    shake.animateTo(14f, tween(45))
                    shake.animateTo(-14f, tween(45))
                }
                shake.animateTo(0f, tween(45))
                pin = ""
            }
        }
    }

    fun onDigit(d: Char) {
        if (isVerified || pin.length >= PIN_LENGTH) return
        errorMessage = null
        pin += d
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(SubtleHighlightOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = CoralOrangePrimary)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 19.sp, color = TextPrimarySlate)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    message,
                    fontSize = 13.sp,
                    color = TextSecondarySlate,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Indikator titik PIN
                Row(
                    modifier = Modifier.offset { IntOffset(shake.value.toInt(), 0) },
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    repeat(PIN_LENGTH) { index ->
                        val filled = index < pin.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        errorMessage != null -> AlertDangerCoral.copy(alpha = 0.25f)
                                        filled -> CoralOrangePrimary
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    2.dp,
                                    if (errorMessage != null) AlertDangerCoral else CoralOrangePrimary,
                                    CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage
                        ?: if (correctPin == DEFAULT_PIN && failedAttempts >= 2) "Petunjuk: PIN bawaan adalah 1234" else " ",
                    color = if (errorMessage != null) AlertDangerCoral else TextSecondarySlate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Keypad angka
                val rows = listOf("123", "456", "789")
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rows.forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { d ->
                                PinKey(label = d.toString(), modifier = Modifier.weight(1f)) { onDigit(d) }
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PinKey(label = "Hapus", modifier = Modifier.weight(1f), small = true) {
                            pin = ""
                            errorMessage = null
                        }
                        PinKey(label = "0", modifier = Modifier.weight(1f)) { onDigit('0') }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.6f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF1F5F9))
                                .clickable(enabled = pin.isNotEmpty() && !isVerified) {
                                    pin = pin.dropLast(1)
                                    errorMessage = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Hapus satu digit",
                                tint = TextPrimarySlate
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                TextButton(onClick = onDismiss) {
                    Text("Batal", color = TextSecondarySlate, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PinKey(
    label: String,
    modifier: Modifier = Modifier,
    small: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1.6f)
            .clip(RoundedCornerShape(16.dp))
            .background(if (small) Color(0xFFF1F5F9) else Color(0xFFF8FAFC))
            .border(1.dp, BorderDividerColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = if (small) 13.sp else 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimarySlate
        )
    }
}
