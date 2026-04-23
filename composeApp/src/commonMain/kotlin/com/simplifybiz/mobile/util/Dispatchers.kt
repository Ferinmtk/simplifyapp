package com.simplifybiz.mobile.util

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Platform-specific I/O dispatcher.
 *
 * On Android, backed by Dispatchers.IO (thread pool tuned for blocking I/O).
 * On iOS, backed by Dispatchers.Default — Kotlin/Native's Dispatchers.IO is
 * marked @internal and isn't safe for common code to reference.
 *
 * Use this everywhere you would have written Dispatchers.IO in commonMain.
 */
expect val ioDispatcher: CoroutineDispatcher