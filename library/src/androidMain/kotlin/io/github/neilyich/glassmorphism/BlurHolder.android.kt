package io.github.neilyich.glassmorphism

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
actual fun checkIfBlurAvailable() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S