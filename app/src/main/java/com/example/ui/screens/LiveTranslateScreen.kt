package com.example.ui.screens

import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AudioActivity
import com.example.data.model.ConnectionStatus
import com.example.data.model.Language
import com.example.ui.components.AudioVisualizerWave
import com.example.ui.components.ConnectionStatusBadge
import com.example.ui.components.LanguageSelectorDialog
import com.example.ui.components.TranscriptCard
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.CleanBengaliAccent
import com.example.ui.theme.CleanBorder
import com.example.ui.theme.CleanDeepNavy
import com.example.ui.theme.CleanDivider
import com.example.ui.theme.CleanEndCallContainer
import com.example.ui.theme.CleanEndCallText
import com.example.ui.theme.CleanPillBlue
import com.example.ui.theme.CleanPillGrey
import com.example.ui.theme.CleanRecordingPulse
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.CleanSurfaceVariant
import com.example.ui.theme.CleanTextMuted
import com.example.ui.theme.CleanTextPrimary
import com.example.ui.theme.CleanTextSecondary
import com.example.ui.viewmodel.KothaViewModel

@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun LiveTranslateScreen(
    viewModel: KothaViewModel,
    modifier: Modifier = Modifier
) {
    val config by viewModel.sessionConfig.collectAsStateWithLifecycle()
    val status by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val activity by viewModel.audioActivity.collectAsStateWithLifecycle()
    val transcripts by viewModel.transcripts.collectAsStateWithLifecycle()
    val visualizerState by viewModel.visualizerState.collectAsStateWithLifecycle()
    val isPlaybackActive by viewModel.audioPlaybackManager.isPlaying.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    var showLanguageOverrideDialog by remember { mutableStateOf(false) }
    var typedInputText by remember { mutableStateOf("") }

    // Auto-scroll on new transcript
    LaunchedEffect(transcripts.size) {
        if (transcripts.isNotEmpty()) {
            listState.animateScrollToItem(transcripts.size - 1)
        }
    }

    if (showLanguageOverrideDialog) {
        LanguageSelectorDialog(
            title = "Override Target Language",
            currentSelected = config.heardLanguage,
            onLanguageSelected = { viewModel.updateConfig(config.copy(heardLanguage = it)) },
            onDismiss = { showLanguageOverrideDialog = false }
        )
    }

    val peerName = when (status) {
        is ConnectionStatus.Connected -> (status as ConnectionStatus.Connected).peerName
        else -> "Sarah (Guest)"
    }

    val peerLang = when (status) {
        is ConnectionStatus.Connected -> (status as ConnectionStatus.Connected).peerLanguage
        else -> config.heardLanguage
    }

    val isCapturing = activity == AudioActivity.CAPTURING_LOCAL
    val isTranslating = activity == AudioActivity.TRANSLATING
    val isPeerPlaying = activity == AudioActivity.PLAYING_PEER_AUDIO || isPlaybackActive

    // Pulse animation for recording
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_halo")
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CleanBackground),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP SECTION (Header & Transcript area)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // TOP BAR: Status, Audio Route & End Call
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ConnectionStatusBadge(
                    status = status,
                    modifier = Modifier.testTag("live_connection_badge")
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Audio Route Button (Speaker vs Earpiece)
                    IconButton(
                        onClick = { viewModel.toggleAudioRouting() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CleanPillBlue)
                            .testTag("audio_route_toggle")
                    ) {
                        Icon(
                            imageVector = if (config.audioRoutingSpeaker) Icons.Default.VolumeUp else Icons.Default.Headset,
                            contentDescription = "Toggle Audio Route",
                            tint = CleanDeepNavy,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // End Session Button
                    IconButton(
                        onClick = { viewModel.endSession() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CleanEndCallContainer)
                            .testTag("end_session_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = CleanEndCallText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // DUAL PARTICIPANT CARD
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CleanBorder, RoundedCornerShape(18.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Local Speaker Info
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isCapturing) CleanRecordingPulse else Color(0xFF22C55E))
                            )
                            Text(
                                text = config.userName,
                                style = MaterialTheme.typography.titleMedium,
                                color = CleanDeepNavy,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${config.spokenLanguage.flag} ${config.spokenLanguage.nativeName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CleanTextSecondary
                        )
                    }

                    // Center Live Status Indicator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(CleanPillGrey)
                            .clickable { showLanguageOverrideDialog = true }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("manual_language_override_trigger")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = when {
                                    isCapturing -> "Speaking…"
                                    isTranslating -> "Translating…"
                                    isPeerPlaying -> "Peer Voice…"
                                    else -> "⇄ Live Pair"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = when {
                                    isCapturing -> CleanRecordingPulse
                                    isTranslating -> CleanBengaliAccent
                                    isPeerPlaying -> CleanBengaliAccent
                                    else -> CleanDeepNavy
                                }
                            )
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Change Language",
                                tint = CleanTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Remote Peer Info
                    Column(horizontalAlignment = Alignment.End) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = peerName,
                                style = MaterialTheme.typography.titleMedium,
                                color = CleanDeepNavy,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isPeerPlaying) CleanBengaliAccent else CleanBorder)
                            )
                        }
                        Text(
                            text = "${peerLang.flag} ${peerLang.englishName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CleanTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // LIVE SCROLLING TRANSCRIPT LIST
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (transcripts.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "কথা বলা শুরু করুন",
                            style = MaterialTheme.typography.titleMedium,
                            color = CleanDeepNavy,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hold the microphone button below to speak. Real-time translation with voice preservation will stream here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleanTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(transcripts, key = { it.id }) { msg ->
                            TranscriptCard(
                                message = msg,
                                onReplayAudio = { viewModel.replayMessageAudio(msg) }
                            )
                        }
                    }
                }
            }
        }

        // BOTTOM SHEET CONTROL AREA (Clean Minimalist White Sheet)
        Surface(
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = CleanSurface,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CleanBorder, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Live Waveform Visualizer
                AudioVisualizerWave(
                    isRecording = isCapturing,
                    isPlaying = isPeerPlaying,
                    amplitude = visualizerState.amplitude,
                    frequencies = visualizerState.waveformFrequencies,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .testTag("audio_waveform_visualizer")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Custom Translation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = typedInputText,
                        onValueChange = { typedInputText = it },
                        placeholder = {
                            Text(
                                text = "Type Bengali or English message…",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleanTextSecondary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CleanDeepNavy,
                            unfocusedBorderColor = CleanBorder,
                            focusedContainerColor = CleanPillGrey,
                            unfocusedContainerColor = CleanPillGrey
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("text_translate_input")
                    )

                    IconButton(
                        onClick = {
                            if (typedInputText.isNotBlank()) {
                                viewModel.injectTestUtterance(typedInputText.trim())
                                typedInputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CleanDeepNavy)
                            .testTag("send_text_translate_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send & Translate",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Quick Preset Bengali Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Language.Bengali.sampleCodeSwitchedPhrases.take(3).forEach { phrase ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(CleanPillGrey)
                                .clickable { viewModel.injectTestUtterance(phrase) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("quick_phrase_${phrase.take(6)}")
                        ) {
                            Text(
                                text = phrase,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = CleanDeepNavy,
                                maxLines = 1
                            )
                        }
                    }

                    // Simulate Peer Turn Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(CleanPillBlue)
                            .clickable { viewModel.triggerSimulatedPeerTurn() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("simulate_peer_turn_button")
                    ) {
                        Text(
                            text = "Peer Reply 🗣️",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = CleanDeepNavy
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // MASSIVE PUSH-TO-TALK BUTTON
                Box(
                    modifier = Modifier
                        .size(92.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Halo glow when capturing
                    if (isCapturing) {
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .scale(haloScale)
                                .clip(CircleShape)
                                .background(CleanRecordingPulse.copy(alpha = 0.22f))
                        )
                    }

                    // Central Tactile PTT Button
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(if (isCapturing) CleanRecordingPulse else CleanDeepNavy)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        if (!isCapturing) {
                                            viewModel.startPushToTalk()
                                        }
                                        val released = tryAwaitRelease()
                                        if (released && isCapturing) {
                                            viewModel.stopPushToTalk()
                                        }
                                    },
                                    onTap = {
                                        if (isCapturing) {
                                            viewModel.stopPushToTalk()
                                        } else {
                                            viewModel.startPushToTalk()
                                        }
                                    }
                                )
                            }
                            .testTag("push_to_talk_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Push to talk",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isCapturing) "RECORDING… TAP OR RELEASE TO TRANSLATE" else "HOLD OR TAP TO SPEAK",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    ),
                    color = if (isCapturing) CleanRecordingPulse else CleanTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
