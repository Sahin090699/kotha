package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TranscriptMessage
import com.example.ui.theme.CleanBorder
import com.example.ui.theme.CleanDeepNavy
import com.example.ui.theme.CleanPillBlue
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.CleanTextMuted
import com.example.ui.theme.CleanTextPrimary
import com.example.ui.theme.CleanTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TranscriptCard(
    message: TranscriptMessage,
    onReplayAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLocal = message.isLocalUser
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    val bubbleShape = if (isLocal) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp),
        horizontalAlignment = if (isLocal) Alignment.End else Alignment.Start
    ) {
        // Tag Header: Language • Time
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (isLocal) "YOU • ${message.sourceLanguage.nativeName.uppercase()} • $formattedTime"
                else "${message.speakerName.uppercase()} • ${message.sourceLanguage.englishName.uppercase()} • $formattedTime",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = if (isLocal) CleanDeepNavy else CleanTextSecondary
            )

            if (message.latencyMs > 0) {
                Text(
                    text = "(${message.latencyMs}ms)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = CleanTextMuted
                )
            }
        }

        // Chat Bubble
        Box(
            modifier = Modifier
                .widthIn(max = 330.dp)
                .clip(bubbleShape)
                .background(if (isLocal) CleanPillBlue else CleanSurface)
                .then(
                    if (!isLocal) Modifier.border(1.dp, CleanBorder, bubbleShape)
                    else Modifier
                )
                .padding(14.dp)
                .testTag("transcript_item_${message.id}")
        ) {
            Column {
                // Original spoken line
                Text(
                    text = message.originalText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = if (isLocal) FontWeight.SemiBold else FontWeight.Normal,
                        lineHeight = 21.sp
                    ),
                    color = CleanTextPrimary
                )

                // Divider between original and translation
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = CleanDeepNavy.copy(alpha = 0.10f),
                    thickness = 1.dp
                )

                // Translated Line
                Text(
                    text = message.translatedText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 20.sp
                    ),
                    color = CleanTextPrimary.copy(alpha = 0.75f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Actions row inside bubble
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Transcript", "${message.originalText}\n${message.translatedText}")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(26.dp).testTag("copy_button_${message.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy text",
                            tint = CleanTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = onReplayAudio,
                        modifier = Modifier.size(26.dp).testTag("replay_button_${message.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Replay audio",
                            tint = CleanDeepNavy,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

