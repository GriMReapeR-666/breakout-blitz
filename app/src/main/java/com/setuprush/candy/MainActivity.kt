package com.setuprush.candy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Compatibility trampoline activity that routes directly to NativeGameActivity.
 * Ensures 100% native execution on all device entry points.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, NativeGameActivity::class.java))
        finish()
    }
}
