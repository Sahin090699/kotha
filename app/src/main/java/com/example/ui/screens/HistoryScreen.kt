package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.local.SessionHistoryEntity
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.CleanBengaliAccent
import com.example.ui.theme.CleanBorder
import com.example.ui.theme.CleanDeepNavy
import com.example.ui.theme.CleanEndCallContainer
import com.example.ui.theme.CleanEndCallText
import com.example.ui.theme.CleanPillBlue
import com.example.ui.theme.CleanPillGrey
import com.example.ui.theme.CleanRecordingPulse
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.CleanTextMuted
import com.example.ui.theme.CleanTextPrimary
import com.example.ui.theme.CleanTextSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.KothaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: KothaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sessions by viewModel.recentSessions.collectAsStateWithLifecycle()
    var selectedSessionId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CleanBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CleanPillGrey)
                        .testTag("history_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CleanDeepNavy,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Conversation History",
                    style = MaterialTheme.typography.titleMedium,
                    color = CleanDeepNavy,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            if (sessions.isNotEmpty()) {
                IconButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        Toast.makeText(context, "All local history deleted", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CleanEndCallContainer)
                        .testTag("clear_all_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear All History",
                        tint = CleanEndCallText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Session List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (sessions.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = CleanTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No saved transcripts",
                        style = MaterialTheme.typography.titleMedium,
                        color = CleanDeepNavy,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Audio transcripts from 1:1 calls will be saved locally on your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CleanTextSecondary,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 6.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sessions, key = { it.sessionId }) { session ->
                        val timeFormat = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())
                        val dateStr = timeFormat.format(Date(session.startTimeMillis))

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CleanSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CleanBorder, RoundedCornerShape(16.dp))
                                .testTag("history_item_${session.sessionId}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Conversation with ${session.peerName}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = CleanDeepNavy,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = dateStr,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CleanTextSecondary
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteSession(session.sessionId) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(CleanPillGrey)
                                            .testTag("delete_session_${session.sessionId}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = CleanEndCallText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(CleanPillBlue)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${session.sourceLanguageCode.uppercase()} ⇄ ${session.targetLanguageCode.uppercase()}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                ),
                                                color = CleanDeepNavy
                                            )
                                        }

                                        Text(
                                            text = "${session.totalUtterances} exchanges",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CleanTextSecondary
                                        )
                                    }

                                    if (session.summary.isNotBlank()) {
                                        Text(
                                            text = "\"${session.summary.take(24)}…\"",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CleanBengaliAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Privacy Data Deletion Control
        Button(
            onClick = {
                viewModel.clearAllHistory()
                Toast.makeText(context, "All session records erased permanently.", Toast.LENGTH_SHORT).show()
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CleanEndCallContainer,
                contentColor = CleanEndCallText
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("delete_all_data_compliance_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = CleanEndCallText)
                Text(
                    text = "Delete My Data (Privacy Compliance)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
