package systems.nzr1

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SystemsApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
