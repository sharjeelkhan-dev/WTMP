package com.sharjeel.wtmp

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
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
import com.sharjeel.wtmp.service.AntiTheftAdminReceiver
import com.sharjeel.wtmp.service.AntiTheftService
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
        
        // CRITICAL: Allow this activity to appear over the system lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        enableEdgeToEdge()

        val isAntiTheftTrigger = intent.getBooleanExtra("TRIGGER_ANTI_THEFT_LOCK", false)
        var isAuthenticated by mutableStateOf(false)

        lifecycleScope.launch {
            val isBiometricEnabled = userPreferencesRepository.isBiometricEnabled.first()

            if (isBiometricEnabled || isAntiTheftTrigger) {
                val title = if (isAntiTheftTrigger) "Anti-Theft Protection" else "Biometric Login"
                val subtitle = if (isAntiTheftTrigger) "Confirm identity to access device options" else "Log in using your biometric credential"

                showBiometricPrompt(title, subtitle) { success ->
                    if (success) {
                        isAuthenticated = true

                        if (isAntiTheftTrigger) {
                            // Notify service that user is owner
                            sendBroadcast(Intent(AntiTheftService.ACTION_AUTHENTICATED).apply {
                                setPackage(packageName)
                            })
                            finishAndRemoveTask()
                        }
                    } else {
                        if (isAntiTheftTrigger) {
                            // User cancelled or failed auth during power intercept
                            sendBroadcast(Intent(AntiTheftService.ACTION_RESET_STATE).apply {
                                setPackage(packageName)
                            })
                            
                            val startMain = Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(startMain)
                            finish()
                        } else {
                            finish()
                        }
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val isAntiTheftTrigger = intent.getBooleanExtra("TRIGGER_ANTI_THEFT_LOCK", false)
        if (isAntiTheftTrigger) {
            showBiometricPrompt("Anti-Theft Protection", "Confirm identity to access device options") { success ->
                if (success) {
                    sendBroadcast(Intent(AntiTheftService.ACTION_AUTHENTICATED).apply {
                        setPackage(packageName)
                    })
                    finishAndRemoveTask()
                } else {
                    sendBroadcast(Intent(AntiTheftService.ACTION_RESET_STATE).apply {
                        setPackage(packageName)
                    })
                    
                    val startMain = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(startMain)
                    finish()
                }
            }
        }
    }

    private fun showBiometricPrompt(
        title: String = "Biometric Login",
        subtitle: String = "Log in using your biometric credential",
        onResult: (Boolean) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If error is cancelled (code 13 or 10), handle it as false result
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
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG 
                    or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun uninstallApp() {
        val dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, AdminReceiver::class.java)
        if (dpm.isAdminActive(adminComponent)) {
            dpm.removeActiveAdmin(adminComponent)
        }

        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = "package:$packageName".toUri()
        }
        startActivity(intent)
    }
}
