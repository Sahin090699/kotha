package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Clean Minimalism Theme Palette
val CleanBackground = Color(0xFFF8F9FF)
val CleanSurface = Color(0xFFFFFFFF)
val CleanSurfaceVariant = Color(0xFFF0F4FA)
val CleanSurfaceElevated = Color(0xFFE9EEF6)
val CleanBorder = Color(0xFFC4C7C5)
val CleanDivider = Color(0xFFE1E2EC)

// Brand & Text Colors
val CleanDeepNavy = Color(0xFF001C35)
val CleanTextPrimary = Color(0xFF001C35)
val CleanTextSecondary = Color(0xFF44474E)
val CleanTextMuted = Color(0xFF74777F)

// Accents & Functional Bubbles
val CleanPillBlue = Color(0xFFD3E3FD)
val CleanPillBlueText = Color(0xFF001C35)
val CleanPillGrey = Color(0xFFE1E2EC)
val CleanEndCallContainer = Color(0xFFF2B8B5)
val CleanEndCallText = Color(0xFF601410)

// Semantic Accents
val CleanBengaliAccent = Color(0xFF0B57D0) // Crisp modern blue
val CleanTargetAccent = Color(0xFF006874) // Crisp teal
val CleanRecordingPulse = Color(0xFFBA1A1A) // Modern red for active recording

// Status Colors
val StatusConnected = Color(0xFF146C2E)
val StatusConnecting = Color(0xFFB58500)
val StatusReconnecting = Color(0xFFD97706)
val StatusDisconnected = Color(0xFFBA1A1A)

// Palette mappings for Clean Minimalism
val MidnightNavy = CleanBackground
val SurfaceDark = CleanSurface
val SurfaceElevated = CleanSurfaceVariant
val SurfaceElevatedBorder = CleanBorder
val BengaliGlow = CleanDeepNavy
val BengaliGlowSubtle = CleanPillBlue
val TargetGlow = CleanBengaliAccent
val TargetGlowSubtle = CleanPillBlue.copy(alpha = 0.4f)
val AccentCoral = CleanRecordingPulse
val AccentCoralSubtle = CleanEndCallContainer
val AccentAmber = Color(0xFFB58500)
val TextPrimary = CleanTextPrimary
val TextSecondary = CleanTextSecondary
val TextMuted = CleanTextMuted
val DividerColor = CleanDivider

