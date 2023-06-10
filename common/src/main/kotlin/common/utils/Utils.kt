package common.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

suspend fun awaitShutdown() {
    withContext(Dispatchers.IO) {
        // Wait for a shutdown signal (e.g., interrupt, termination signal, etc.)
        // You can customize this code based on your specific shutdown mechanism
        while (isActive) {
            delay(100)
        }
    }
}