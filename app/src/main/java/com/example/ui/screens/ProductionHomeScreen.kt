package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.KothaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProductionHomeScreen(viewModel: KothaViewModel, modifier: Modifier = Modifier) {
    val config by viewModel.sessionConfig.collectAsStateWithLifecycle()
    val sessions by viewModel.recentSessions.collectAsStateWithLifecycle()
    val showPrivacy by viewModel.showPrivacyDialog.collectAsStateWithLifecycle()
    var sourceDialog by remember { mutableStateOf(false) }
    var targetDialog by remember { mutableStateOf(false) }

    if (showPrivacy) PrivacyConsentDialog(
        onAccept = { viewModel.setPrivacyConsent(true) },
        onDecline = { viewModel.setPrivacyConsent(false) }
    )
    if (sourceDialog) LanguageSelectorDialog(
        title = "Your language", currentSelected = config.spokenLanguage,
        onLanguageSelected = { viewModel.updateConfig(config.copy(spokenLanguage = it)); sourceDialog = false },
        onDismiss = { sourceDialog = false }
    )
    if (targetDialog) LanguageSelectorDialog(
        title = "Translation language", currentSelected = config.heardLanguage,
        onLanguageSelected = { viewModel.updateConfig(config.copy(heardLanguage = it)); targetDialog = false },
        onDismiss = { targetDialog = false }
    )

    LazyColumn(
        modifier = modifier.fillMaxSize().background(CleanBackground).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(CircleShape).background(CleanDeepNavy), contentAlignment = Alignment.Center) {
                        Text("ক", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Kotha", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CleanTextPrimary)
                        Text("Real-time speech interpreter", fontSize = 12.sp, color = CleanTextSecondary)
                    }
                }
                Row {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.HISTORY) }, modifier = Modifier.testTag("history_nav_button")) { Icon(Icons.Default.History, "History") }
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.SETTINGS) }, modifier = Modifier.testTag("settings_nav_button")) { Icon(Icons.Default.Settings, "Settings") }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = CleanDeepNavy)) {
                Column(Modifier.padding(24.dp)) {
                    Text("SPEAK NATURALLY", color = CleanPillBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Translate conversations\nwithout breaking the flow.", color = Color.White, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Text("Gemini Live listens, interprets and speaks the translation back in real time.", color = Color.White.copy(alpha = .78f), fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth().border(1.dp, CleanBorder, RoundedCornerShape(22.dp)), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = CleanSurface)) {
                Column(Modifier.padding(18.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("LANGUAGES", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, color = CleanTextSecondary)
                        Icon(Icons.Default.Language, null, tint = CleanBengaliAccent)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LanguageCard("FROM", config.spokenLanguage.flag, config.spokenLanguage.nativeName, Modifier.weight(1f)) { sourceDialog = true }
                        IconButton(onClick = { viewModel.updateConfig(config.copy(spokenLanguage = config.heardLanguage, heardLanguage = config.spokenLanguage)) }, Modifier.padding(horizontal = 4.dp).size(42.dp).border(1.dp, CleanBorder, CircleShape)) { Icon(Icons.Default.SwapHoriz, "Swap languages") }
                        LanguageCard("TO", config.heardLanguage.flag, config.heardLanguage.nativeName, Modifier.weight(1f)) { targetDialog = true }
                    }
                }
            }
        }
        item {
            Button(onClick = { viewModel.startDirectLiveSession() }, Modifier.fillMaxWidth().height(62.dp).testTag("start_conversation_button"), RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = CleanBengaliAccent)) {
                Icon(Icons.Default.Mic, null, Modifier.size(22.dp)); Spacer(Modifier.width(10.dp)); Text("Start live translation", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Recent conversations", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = CleanTextPrimary)
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = { viewModel.navigateTo(AppScreen.HISTORY) }, shape = RoundedCornerShape(12.dp)) { Text("See all") }
            }
        }
        if (sessions.isEmpty()) item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = CleanSurfaceVariant)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No conversations yet", fontWeight = FontWeight.SemiBold, color = CleanTextPrimary)
                    Spacer(Modifier.height(4.dp)); Text("Completed translations will appear here.", color = CleanTextSecondary, fontSize = 13.sp)
                }
            }
        } else items(sessions.take(3)) { session ->
            Card(Modifier.fillMaxWidth().border(1.dp, CleanBorder, RoundedCornerShape(18.dp)), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = CleanSurface)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(CleanPillBlue), contentAlignment = Alignment.Center) { Text(session.sourceLanguageCode.uppercase(Locale.US).take(2), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CleanDeepNavy) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(session.peerName, fontWeight = FontWeight.SemiBold, color = CleanTextPrimary)
                        Text("${session.sourceLanguageCode.uppercase()} → ${session.targetLanguageCode.uppercase()} • ${SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(session.startTimeMillis))}", fontSize = 12.sp, color = CleanTextSecondary)
                    }
                    Text("${session.totalUtterances}", fontSize = 12.sp, color = CleanTextSecondary)
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun LanguageCard(label: String, flag: String, language: String, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(CleanSurfaceVariant).clickable(onClick = onClick).padding(13.dp)) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CleanTextSecondary, letterSpacing = 1.sp)
        Spacer(Modifier.height(5.dp))
        Row(verticalAlignment = Alignment.CenterVertically) { Text(flag, fontSize = 20.sp); Spacer(Modifier.width(7.dp)); Text(language, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CleanTextPrimary) }
    }
}
