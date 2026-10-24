package app.adhantv

import android.app.Application
import app.adhantv.ui.theme.Palette
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AdhanApp : Application() {
    val container by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        // Every screen reads its colours from Palette; keep it on the chosen theme.
        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            container.settings.settings.map { it.theme }.distinctUntilChanged().collect { Palette.theme = it }
        }
    }
}
