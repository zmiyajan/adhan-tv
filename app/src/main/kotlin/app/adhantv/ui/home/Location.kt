package app.adhantv.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * One approximate fix from the TV's network location (Wi-Fi via Google), used once to pick the nearest city.
 * Returns null if the TV has no location service or it does not answer in time. Call only with the permission.
 */
@SuppressLint("MissingPermission")
suspend fun approximateLocation(context: Context): Location? {
    val lm = context.getSystemService(LocationManager::class.java)
    val providers = listOf(LocationManager.NETWORK_PROVIDER, "fused", LocationManager.PASSIVE_PROVIDER)   // "fused" exists from API 31
        .filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        for (p in providers.take(2)) {
            val fix = withTimeoutOrNull(15_000) {
                suspendCancellableCoroutine { cont ->
                    val cancel = CancellationSignal()
                    cont.invokeOnCancellation { cancel.cancel() }
                    lm.getCurrentLocation(p, cancel, context.mainExecutor) { cont.resume(it) }
                }
            }
            if (fix != null) return fix
        }
    }
    return providers.mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }.maxByOrNull { it.time }
}
