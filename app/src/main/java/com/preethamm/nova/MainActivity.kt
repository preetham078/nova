package com.preethamm.nova

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.preethamm.nova.conversation.NovaViewModel
import com.preethamm.nova.navigation.NovaApp
import com.preethamm.nova.ui.theme.NovaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = NovaAppContainer(applicationContext)

        setContent {
            NovaTheme {
                val viewModel: NovaViewModel = viewModel(
                    factory = NovaViewModel.Factory(container)
                )
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = viewModel::setMicrophonePermission
                )
                val hasMicPermission = remember {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                }

                LaunchedEffect(Unit) {
                    viewModel.setMicrophonePermission(hasMicPermission)
                    if (!hasMicPermission) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }

                NovaApp(
                    state = state,
                    onMicPressed = viewModel::onMicPressed,
                    onStopPressed = viewModel::stop,
                    onTypedTextChanged = viewModel::onTypedTextChanged,
                    onSendText = viewModel::sendTypedMessage,
                    onClearConversation = viewModel::clearConversation,
                    onApiKeyDraftChanged = viewModel::onApiKeyDraftChanged,
                    onSaveApiKey = viewModel::saveApiKey,
                    onClearApiKey = viewModel::clearApiKey,
                    onRequestMicPermission = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )
            }
        }
    }
}
