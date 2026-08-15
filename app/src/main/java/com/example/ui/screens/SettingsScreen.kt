package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.LanguageSelectorDialog
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

@Composable
fun SettingsScreen(
    viewModel: KothaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val config by viewModel.sessionConfig.collectAsStateWithLifecycle()

    var showSpokenDialog by remember { mutableStateOf(false) }
    var showHeardDialog by remember { mutableStateOf(false) }

    if (showSpokenDialog) {
        LanguageSelectorDialog(
            title = "Your Spoken Dialect",
            currentSelected = config.spokenLanguage,
            onLanguageSelected = { viewModel.updateConfig(config.copy(spokenLanguage = it)) },
            onDismiss = { showSpokenDialog = false }
        )
    }

    if (showHeardDialog) {
        LanguageSelectorDialog(
            title = "Listener Translation Dialect",
            currentSelected = config.heardLanguage,
            onLanguageSelected = { viewModel.updateConfig(config.copy(heardLanguage = it)) },
            onDismiss = { showHeardDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CleanBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar
        item {
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
                        .testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CleanDeepNavy,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Settings & Audio Config",
                    style = MaterialTheme.typography.titleMedium,
                    color = CleanDeepNavy,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }

        // Profile Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CleanBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = CleanDeepNavy, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Speaker Profile",
                            style = MaterialTheme.typography.titleSmall,
                            color = CleanDeepNavy,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = config.userName,
                        onValueChange = { viewModel.updateConfig(config.copy(userName = it)) },
                        label = { Text("Display Name (shown to peer)", color = CleanTextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CleanDeepNavy,
                            unfocusedBorderColor = CleanBorder,
                            focusedTextColor = CleanDeepNavy,
                            unfocusedTextColor = CleanDeepNavy,
                            focusedContainerColor = CleanSurface,
                            unfocusedContainerColor = CleanSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_settings_input")
                    )
                }
            }
        }

        // Audio & Engine Tuning
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CleanBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = CleanDeepNavy, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Audio & Gemini Engine Tuning",
                            style = MaterialTheme.typography.titleSmall,
                            color = CleanDeepNavy,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Voice Pitch & Style Transfer Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Voice Style & Pitch Preservation",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CleanDeepNavy,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Matches synthesized translated speech to original speaker's pitch and cadence",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleanTextSecondary
                            )
                        }

                        Switch(
                            checked = config.voicePitchTransferEnabled,
                            onCheckedChange = { viewModel.updateConfig(config.copy(voicePitchTransferEnabled = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CleanSurface,
                                checkedTrackColor = CleanDeepNavy,
                                uncheckedThumbColor = CleanTextMuted,
                                uncheckedTrackColor = CleanPillGrey
                            ),
                            modifier = Modifier.testTag("pitch_preservation_switch")
                        )
                    }

                    // Noise Gate Threshold Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Noise Gate Sensitivity",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CleanDeepNavy,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${(config.noiseGateThreshold * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = CleanDeepNavy
                            )
                        }
                        Text(
                            text = "Filters out ambient background chatter and road noise before streaming audio",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleanTextSecondary
                        )
                        Slider(
                            value = config.noiseGateThreshold,
                            onValueChange = { viewModel.updateConfig(config.copy(noiseGateThreshold = it)) },
                            valueRange = 0.01f..0.10f,
                            colors = SliderDefaults.colors(
                                thumbColor = CleanDeepNavy,
                                activeTrackColor = CleanDeepNavy,
                                inactiveTrackColor = CleanPillGrey
                            ),
                            modifier = Modifier.testTag("noise_gate_slider")
                        )
                    }

                    // Audio Route Default
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Default Audio Output",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CleanDeepNavy,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (config.audioRoutingSpeaker) "Speakerphone (Loudspeaker)" else "Earpiece (Private call mode)",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleanTextSecondary
                            )
                        }

                        Switch(
                            checked = config.audioRoutingSpeaker,
                            onCheckedChange = { viewModel.updateConfig(config.copy(audioRoutingSpeaker = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CleanSurface,
                                checkedTrackColor = CleanDeepNavy,
                                uncheckedThumbColor = CleanTextMuted,
                                uncheckedTrackColor = CleanPillGrey
                            ),
                            modifier = Modifier.testTag("audio_route_switch")
                        )
                    }
                }
            }
        }

        // Privacy & Architecture Info
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CleanBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = CleanDeepNavy, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Privacy Architecture",
                            style = MaterialTheme.typography.titleSmall,
                            color = CleanDeepNavy,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "• Model: Google Gemini 3.5 Live Translate Engine\n• Zero raw audio retention server-side\n• Transcripts encrypted and stored locally in on-device SQLite database\n• WebSocket audio relay with automatic session teardown",
                        style = MaterialTheme.typography.bodySmall,
                        color = CleanTextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
