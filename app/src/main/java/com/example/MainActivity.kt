package com.example

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HostPairingScreen
import com.example.ui.screens.JoinPairingScreen
import com.example.ui.screens.LiveTranslateScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.KothaViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: KothaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MyApplicationTheme { KothaApp(viewModel) } }
    }
}

@Composable
fun KothaApp(viewModel: KothaViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> viewModel.setPrivacyConsent(granted) }

    LaunchedEffect(currentScreen) {
        if (currentScreen == AppScreen.ACTIVE_TRANSLATE &&
            ContextCompat.checkSelfPermission(
                viewModel.getApplication<Application>(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        if (currentScreen == AppScreen.ACTIVE_TRANSLATE) viewModel.endSession()
        else viewModel.navigateTo(AppScreen.HOME)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when (currentScreen) {
            AppScreen.HOME -> HomeScreen(viewModel, Modifier.padding(innerPadding))
            AppScreen.HOST_PAIRING -> HostPairingScreen(viewModel, Modifier.padding(innerPadding))
            AppScreen.JOIN_PAIRING -> JoinPairingScreen(viewModel, Modifier.padding(innerPadding))
            AppScreen.ACTIVE_TRANSLATE -> LiveTranslateScreen(viewModel, Modifier.padding(innerPadding))
            AppScreen.HISTORY -> HistoryScreen(viewModel, Modifier.padding(innerPadding))
            AppScreen.SETTINGS -> SettingsScreen(viewModel, Modifier.padding(innerPadding))
        }
    }
}
