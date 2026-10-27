package com.yadyar.app.security

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.yadyar.app.ui.components.GradientBackground
import com.yadyar.app.ui.theme.GoldAccent
import com.yadyar.app.ui.theme.TextPrimary
import com.yadyar.app.ui.theme.TextSecondary

@Composable
fun PinLockScreen(onUnlocked: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? FragmentActivity
    val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    val biometricAvailable = activity?.let { BiometricManager.from(it).canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS } == true
    val prompt = remember(activity) {
        activity?.let { host ->
            BiometricPrompt(host, ContextCompat.getMainExecutor(host), object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { onUnlocked() }
                override fun onAuthenticationFailed() { error = true }
                override fun onAuthenticationError(code: Int, message: CharSequence) { if (code != BiometricPrompt.ERROR_USER_CANCELED && code != BiometricPrompt.ERROR_NEGATIVE_BUTTON) error = true }
            })
        }
    }
    GradientBackground {
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.Center) {
            Text("MONO", style=MaterialTheme.typography.headlineLarge, color=TextPrimary, fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            val patternMode = AppLock.isPattern(context)
            Text(if (patternMode) "الگوی ورود را رسم کنید" else "برای ورود رمز را وارد کنید", color=TextSecondary)
            Spacer(Modifier.height(20.dp))
            if (patternMode) {
                Text("الگو: $pin", color=TextPrimary)
                Spacer(Modifier.height(10.dp))
                (0..2).forEach { row ->
                    Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                        (1..3).forEach { col ->
                            val n = row*3+col
                            OutlinedButton(onClick={ if (!pin.contains(n.toString())) pin += n.toString(); error=false }, modifier=Modifier.size(64.dp)) { Text(n.toString(), color=TextPrimary) }
                        }
                    }
                }
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick={pin=""; error=false}) { Text("پاک‌کردن", color=TextSecondary) }
                    Button(onClick={if(pin.length>=4 && AppLock.verify(context,pin)) onUnlocked() else error=true}, colors=ButtonDefaults.buttonColors(containerColor=GoldAccent)) { Text("تأیید", color=androidx.compose.ui.graphics.Color.Black) }
                }
            } else {
                OutlinedTextField(value=pin, onValueChange={ pin=it.filter(Char::isDigit).take(12); error=false }, label={Text("رمز عبور")}, keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword), singleLine=true)
                Spacer(Modifier.height(14.dp))
                Button(onClick={ if (AppLock.verify(context,pin)) onUnlocked() else error=true }, colors=ButtonDefaults.buttonColors(containerColor=GoldAccent)) { Text("ورود با PIN", color=androidx.compose.ui.graphics.Color.Black) }
            }
            if (error) Text("رمز یا تأیید هویت ناموفق بود", color=MaterialTheme.colorScheme.error)
            if (biometricAvailable) TextButton(onClick={
                error=false
                prompt?.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("ورود به MONO").setSubtitle("اثر انگشت یا قفل دستگاه").setAllowedAuthenticators(authenticators).build())
            }) { Text("ورود با اثر انگشت / قفل دستگاه", color=GoldAccent) }
        }
    }
}
