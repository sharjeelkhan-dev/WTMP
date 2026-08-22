package com.sharjeel.wtmp

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.sharjeel.wtmp.repository.UserPreferencesRepository
import com.sharjeel.wtmp.service.AdminReceiver
import com.sharjeel.wtmp.ui.navigation.NavGraph
import com.sharjeel.wtmp.ui.screens.settings.SettingsViewModel
import com.sharjeel.wtmp.ui.theme.WTMPTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var isAuthenticated by mutableStateOf(false)

        lifecycleScope.launch {
            val isBiometricEnabled = userPreferencesRepository.isBiometricEnabled.first()

            if (isBiometricEnabled) {
                showBiometricPrompt { success ->
                    if (success) {
                        isAuthenticated = true
                    } else {
                        finish()
                    }
                }
            } else {
                isAuthenticated = true
            }
        }

        setContent {
            val settingsUiState by settingsViewModel.uiState.collectAsState()

            val useDarkTheme = when (settingsUiState.themeMode) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            WTMPTheme(darkTheme = useDarkTheme) {
                if (isAuthenticated) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        userPreferencesRepository = userPreferencesRepository
                    )
                }
            }
        }
    }

    private fun showBiometricPrompt(onResult: (Boolean) -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                    onResult(false)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onResult(true)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    // Helper method to safely uninstall WTMP app
    fun uninstallApp() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, AdminReceiver::class.java) // Aapka DeviceAdminReceiver Class Name

        if (dpm.isAdminActive(adminComponent)) {
            dpm.removeActiveAdmin(adminComponent) // Programmatically Remove Device Admin
        }

        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = "package:$packageName".toUri()
        }
        startActivity(intent)
    }
}