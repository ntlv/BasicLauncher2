package se.ntlv.basiclauncher.dagger

import dagger.Component
import se.ntlv.basiclauncher.packagehandling.AppChangeLoggerService
import se.ntlv.basiclauncher.MainActivity
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {

    companion object {
        fun init(module: ApplicationModule) = DaggerApplicationComponent.builder()
                .applicationModule(module)
                .build()

    }

    fun inject(target : AppChangeLoggerService)

    fun inject(target : MainActivity)
}

