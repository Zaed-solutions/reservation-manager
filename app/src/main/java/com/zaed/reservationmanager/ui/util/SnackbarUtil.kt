package com.zaed.reservationmanager.ui.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Extension function to show a Snackbar with a custom duration in milliseconds.
 *
 * @param message The message to display in the Snackbar.
 * @param durationMillis The custom duration in milliseconds for which the Snackbar will be visible.
 */
fun SnackbarHostState.showSnackbarWithDuration(
    message: String,
    durationMillis: Long,
    scope: CoroutineScope,
    onFinished: () -> Unit = {}
) {
    scope.launch {
        val job = launch {
            showSnackbar(
                message = message,
                duration = SnackbarDuration.Indefinite
            )
        }
        // Delay for the custom duration
        delay(durationMillis)
        job.cancel() // Cancels the Snackbar coroutine, effectively hiding it
        onFinished()
    }
}

