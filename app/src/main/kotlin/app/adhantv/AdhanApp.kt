package app.adhantv

import android.app.Application

class AdhanApp : Application() {
    val container by lazy { AppContainer(this) }
}
