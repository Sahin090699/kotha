package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.QrCodeView
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.CleanBengaliAccent
import com.example.ui.theme.CleanBorder
import com.example.ui.theme.CleanDeepNavy
import com.example.ui.theme.CleanPillBlue
import com.example.ui.theme.CleanPillGrey
import com.example.ui.theme.CleanRecordingPulse
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.CleanTextMuted
import com.example.ui.theme.CleanTextPrimary
import com.example.ui.theme.CleanTextSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.KothaViewModel
import kotlinx.coroutines.delay

@Composable
fun HostPairingScreen(
    viewModel: KothaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pairingInfo by viewModel.pairingInfo.collectAsStateWithLifecycle()
    val sessionCode = pairingInfo?.sessionCode ?: "KT7B29"
    var secondsLeft by remember { mutableLongStateOf(pairingInfo?.remainingSeconds ?: 600L) }

    LaunchedEffect(pairingInfo) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft = pairingInfo?.remainingSeconds ?: 0L
        }
    }

    val minutes = secondsLeft / 60
    val secs = secondsLeft % 60
    val formattedTime = String.format("%02d:%02d", minutes, secs)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CleanBackground)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CleanPillGrey)
                    .testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = CleanDeepNavy,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "Pairing Session",
                style = MaterialTheme.typography.titleMedium,
                color = CleanDeepNavy,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        // Center Content: QR Code & 6-Char Session Code
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Ask the guest to scan or enter the session code",
                style = MaterialTheme.typography.bodyMedium,
                color = CleanTextSecondary,
                textAlign = TextAlign.Center
            )

            // QR Code Box
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                modifier = Modifier
                    .border(1.dp, CleanBorder, RoundedCornerShape(20.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier.padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    QrCodeView(
                        payload = pairingInfo?.qrPayload ?: "kotha://pair?code=$sessionCode",
                        size = 180.dp,
                        modifier = Modifier.testTag("qr_code_display")
                    )
                }
            }

            // Large Alphanumeric Code Display
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CleanBorder, RoundedCornerShape(16.dp))
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Session Code", sessionCode)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Code copied: $sessionCode", Toast.LENGTH_SHORT).show()
                    }
                    .testTag("session_code_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "SESSION CODE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = CleanTextSecondary
                        )
                        Text(
                            text = "${sessionCode.take(3)} - ${sessionCode.drop(3)}",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = CleanDeepNavy
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = CleanDeepNavy,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Expiration pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CleanPillGrey)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(CleanRecordingPulse)
                )
                Text(
                    text = "Code expires in $formattedTime",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = CleanTextSecondary
                )
            }
        }

        // Bottom Actions
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Simulated Peer Shortcut for instant testing
            Button(
                onClick = { viewModel.simulatePeerJoinInHost() },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CleanPillBlue,
                    contentColor = CleanDeepNavy
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("simulate_peer_join_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CleanDeepNavy)
                    Text("Instant Test: Connect Simulated Peer", fontWeight = FontWeight.SemiBold)
                }
            }

            Text(
                text = "Awaiting participant connection…",
                style = MaterialTheme.typography.bodySmall,
                color = CleanTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun JoinPairingScreen(
    viewModel: KothaViewModel,
    modifier: Modifier = Modifier
) {
    var enteredCode by remember { mutableStateOf("") }
    val isCodeValid = enteredCode.trim().length >= 6

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CleanBackground)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CleanPillGrey)
                    .testTag("join_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = CleanDeepNavy,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "Join Conversation",
                style = MaterialTheme.typography.titleMedium,
                color = CleanDeepNavy,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        // Center Input
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Enter the 6-character code from the host",
                style = MaterialTheme.typography.bodyMedium,
                color = CleanTextSecondary,
                textAlign = TextAlign.Center
            )

            OutlinedTextField(
                value = enteredCode,
                onValueChange = {
                    if (it.length <= 6) {
                        enteredCode = it.uppercase()
                    }
                },
                placeholder = { Text("e.g. KT7B29", color = CleanTextMuted, fontFamily = FontFamily.Monospace) },
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = CleanDeepNavy,
                    letterSpacing = 4.sp
                ),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CleanDeepNavy,
                    unfocusedBorderColor = CleanBorder,
                    focusedContainerColor = CleanSurface,
                    unfocusedContainerColor = CleanSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("code_input_field")
            )

            // Quick Preset demo codes
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Or tap a quick demo room:",
                    style = MaterialTheme.typography.labelSmall,
                    color = CleanTextMuted
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("KT7B29", "BN9X21", "SIMU2W").forEach { sampleCode ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(CleanPillGrey)
                                .clickable { enteredCode = sampleCode }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("demo_code_$sampleCode")
                        ) {
                            Text(
                                text = sampleCode,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = CleanDeepNavy,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Join Action Button
        Button(
            onClick = {
                viewModel.joinWithCode(enteredCode.ifBlank { "KT7B29" }, isSimulation = true)
            },
            enabled = isCodeValid || enteredCode.isNotBlank(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CleanDeepNavy,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("confirm_join_button")
        ) {
            Text(
                text = "Connect to Session",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
