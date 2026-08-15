package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.LanguageSelectorDialog
import com.example.ui.components.PrivacyConsentDialog
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.CleanBengaliAccent
import com.example.ui.theme.CleanBorder
import com.example.ui.theme.CleanDeepNavy
import com.example.ui.theme.CleanDivider
import com.example.ui.theme.CleanPillBlue
import com.example.ui.theme.CleanPillGrey
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.CleanSurfaceElevated
import com.example.ui.theme.CleanSurfaceVariant
import com.example.ui.theme.CleanTextMuted
import com.example.ui.theme.CleanTextPrimary
import com.example.ui.theme.CleanTextSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.KothaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: KothaViewModel,
    modifier: Modifier = Modifier
) {
    val config by viewModel.sessionConfig.collectAsStateWithLifecycle()
    val recentSessions by viewModel.recentSessions.collectAsStateWithLifecycle()
    val showPrivacy by viewModel.showPrivacyDialog.collectAsStateWithLifecycle()

    var showSpokenLangDialog by remember { mutableStateOf(false) }
    var showHeardLangDialog by remember { mutableStateOf(false) }

    if (showPrivacy) {
        PrivacyConsentDialog(
            onAccept = { viewModel.setPrivacyConsent(true) },
            onDecline = { viewModel.setPrivacyConsent(false) }
        )
    }

    if (showSpokenLangDialog) {
        LanguageSelectorDialog(
            title = "Your Spoken Language",
            currentSelected = config.spokenLanguage,
            onLanguageSelected = { viewModel.updateConfig(config.copy(spokenLanguage = it)) },
            onDismiss = { showSpokenLangDialog = false }
        )
    }

    if (showHeardLangDialog) {
        LanguageSelectorDialog(
            title = "Listener / Target Language",
            currentSelected = config.heardLanguage,
            onLanguageSelected = { viewModel.updateConfig(config.copy(heardLanguage = it)) },
            onDismiss = { showHeardLangDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CleanBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header Bar
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CleanPillBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ক",
                            color = CleanDeepNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }

                    Column {
                        Text(
                            text = "Kotha",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = CleanDeepNavy
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                            Text(
                                text = "BENGALI ⇄ SPEECH",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 10.sp
                                ),
                                color = CleanTextSecondary
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.HISTORY) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CleanPillBlue)
                            .testTag("history_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = CleanDeepNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CleanPillBlue)
                            .testTag("settings_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = CleanDeepNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Hero Card with active Language Pair
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CleanBorder, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE LANGUAGE PAIR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = CleanTextSecondary
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(CleanPillGrey)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Code-Switching",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = CleanDeepNavy
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Spoken & Heard Languages Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // User Language Card
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CleanSurfaceVariant)
                                .border(1.dp, CleanBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .clickable { showSpokenLangDialog = true }
                                .padding(12.dp)
                                .testTag("spoken_language_selector")
                        ) {
                            Text(
                                text = "FROM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.8.sp
                                ),
                                color = CleanTextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(config.spokenLanguage.flag, fontSize = 20.sp)
                                Text(
                                    config.spokenLanguage.nativeName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CleanDeepNavy,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Swap Icon Button
                        IconButton(
                            onClick = {
                                val prevSpoken = config.spokenLanguage
                                val prevHeard = config.heardLanguage
                                viewModel.updateConfig(
                                    config.copy(
                                        spokenLanguage = prevHeard,
                                        heardLanguage = prevSpoken
                                    )
                                )
                            },
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .size(40.dp)
                                .border(1.dp, CleanBorder, CircleShape)
                                .testTag("swap_languages_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Swap Languages",
                                tint = CleanTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Listener Language Card
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CleanSurfaceVariant)
                                .border(1.dp, CleanBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .clickable { showHeardLangDialog = true }
                                .padding(12.dp)
                                .testTag("heard_language_selector")
                        ) {
                            Text(
                                text = "TO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.8.sp
                                ),
                                color = CleanTextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(config.heardLanguage.flag, fontSize = 20.sp)
                                Text(
                                    config.heardLanguage.englishName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CleanDeepNavy,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Primary Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Direct Instant Live Voice Translation Button
                Button(
                    onClick = { viewModel.startDirectLiveSession() },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CleanDeepNavy,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("start_conversation_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(22.dp))
                        Text(
                            text = "Start Live Voice Translation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Host Button
                    OutlinedButton(
                        onClick = { viewModel.startHostSession() },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CleanDeepNavy
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .border(1.dp, CleanBorder, RoundedCornerShape(16.dp))
                            .testTag("host_session_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CleanDeepNavy, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Host 1:1",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Join Button
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppScreen.JOIN_PAIRING) },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CleanDeepNavy
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .border(1.dp, CleanBorder, RoundedCornerShape(16.dp))
                            .testTag("join_conversation_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null, tint = CleanDeepNavy, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Join Code",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Interactive 2-Way Simulation Mode
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.joinWithCode("SIMU2W", isSimulation = true) }
                        .border(1.dp, CleanBorder, RoundedCornerShape(18.dp))
                        .testTag("interactive_simulation_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(CleanPillBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = CleanDeepNavy,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "2-Party Interactive Simulation",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = CleanTextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Test Bengali code-switching live on device",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CleanTextSecondary
                                )
                            }
                        }

                        Text(
                            text = "Try →",
                            style = MaterialTheme.typography.labelLarge,
                            color = CleanBengaliAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Recent Sessions Section
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Sessions",
                    style = MaterialTheme.typography.titleMedium,
                    color = CleanTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                if (recentSessions.isNotEmpty()) {
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelSmall,
                        color = CleanBengaliAccent,
                        modifier = Modifier
                            .clickable { viewModel.navigateTo(AppScreen.HISTORY) }
                            .padding(4.dp)
                    )
                }
            }
        }

        if (recentSessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(CleanSurface)
                        .border(1.dp, CleanBorder, RoundedCornerShape(18.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = CleanTextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "No saved sessions yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CleanTextSecondary
                        )
                        Text(
                            text = "Start a 1:1 conversation to see transcripts here",
                            style = MaterialTheme.typography.labelSmall,
                            color = CleanTextMuted
                        )
                    }
                }
            }
        } else {
            items(recentSessions.take(4)) { session ->
                val timeFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                val dateStr = timeFormat.format(Date(session.startTimeMillis))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CleanBorder, RoundedCornerShape(16.dp))
                        .clickable { viewModel.navigateTo(AppScreen.HISTORY) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "With ${session.peerName}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = CleanTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${session.sourceLanguageCode.uppercase()} ⇄ ${session.targetLanguageCode.uppercase()} • ${session.totalUtterances} exchanges",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleanTextSecondary
                            )
                        }

                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = CleanTextMuted
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.showPrivacyConsent() }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = CleanTextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Zero Cloud Audio Retention • 100% Private",
                    style = MaterialTheme.typography.labelSmall,
                    color = CleanTextMuted
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

