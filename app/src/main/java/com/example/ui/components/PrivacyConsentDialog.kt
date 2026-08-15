package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CleanBorder
import com.example.ui.theme.CleanDeepNavy
import com.example.ui.theme.CleanPillBlue
import com.example.ui.theme.CleanPillGrey
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.CleanTextMuted
import com.example.ui.theme.CleanTextPrimary
import com.example.ui.theme.CleanTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyConsentDialog(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDecline) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CleanSurface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, CleanBorder, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Privacy & Consent",
                        tint = CleanDeepNavy,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Privacy & Audio Consent",
                        style = MaterialTheme.typography.titleLarge,
                        color = CleanDeepNavy,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Kotha provides live speech-to-speech translation between Bengali and world languages with vocal style preservation.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CleanDeepNavy,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CleanPillBlue)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = CleanDeepNavy, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Microphone audio is streamed only while holding push-to-talk to transcribe and translate.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleanDeepNavy
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = CleanDeepNavy, modifier = Modifier.size(18.dp))
                        Text(
                            text = "No audio recordings are permanently stored on third-party servers. All session histories are stored strictly locally on your device with 1-click deletion.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleanDeepNavy
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("privacy_decline_button")
                    ) {
                        Text("Decline", color = CleanTextSecondary)
                    }

                    Button(
                        onClick = onAccept,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CleanDeepNavy,
                            contentColor = CleanSurface
                        ),
                        modifier = Modifier.weight(1f).testTag("privacy_accept_button")
                    ) {
                        Text("I Agree", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
