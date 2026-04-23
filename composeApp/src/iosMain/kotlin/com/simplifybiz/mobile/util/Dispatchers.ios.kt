package com.simplifybiz.mobile.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

// Kotlin/Native's Dispatchers.IO is internal — use Default on iOS.
// Default is safe for the Room and Ktor work we do off the main thread.
actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default