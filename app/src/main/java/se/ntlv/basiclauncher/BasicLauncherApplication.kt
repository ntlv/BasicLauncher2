package se.ntlv.basiclauncher

import android.app.Application
import se.ntlv.basiclauncher.dagger.ApplicationComponent
import se.ntlv.basiclauncher.dagger.ApplicationModule

class BasicLauncherApplication : Application() {

    companion object {
        @JvmStatic lateinit var graph: ApplicationComponent

        fun applicationComponent() = graph
    }

    override fun onCreate() {
        super.onCreate()
        graph = ApplicationComponent.init(ApplicationModule(this))
    }
}
