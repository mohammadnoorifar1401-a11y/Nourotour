package com.yadyar.app

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.yadyar.app.security.AppLock
import com.yadyar.app.security.PinLockScreen
import com.yadyar.app.ui.navigation.AppNavGraph
import com.yadyar.app.ui.theme.YadYarTheme

class MainActivity : FragmentActivity() {
    private var requestLockOnStop: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var unlocked by remember { mutableStateOf(!AppLock.isEnabled(this@MainActivity)) }
            requestLockOnStop = { unlocked = false }
            YadYarTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (unlocked) AppNavGraph() else PinLockScreen { unlocked = true }
                }
            }
        }
    }

    override fun onStop() {
        if (AppLock.isEnabled(this)) requestLockOnStop?.invoke()
        super.onStop()
    }
}
