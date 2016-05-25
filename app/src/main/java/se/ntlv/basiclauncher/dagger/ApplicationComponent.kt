package se.ntlv.basiclauncher.dagger

import android.app.Application
import android.content.pm.PackageManager
import dagger.Component
import se.ntlv.basiclauncher.packagehandling.AppChangeLoggerService
import se.ntlv.basiclauncher.repository.AppDetailDB
import java.io.File
import javax.inject.Named

@ApplicationScope
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {

    object Init {
        fun init(module: ApplicationModule): ApplicationComponent {
            return DaggerApplicationComponent.builder()
                    .applicationModule(module)
                    .build()
        }
    }

    fun application(): Application

    fun db() : AppDetailDB

    fun packageManager() : PackageManager

    @Named("cache")
    fun cache() : File

    @Named("version")
    fun version() : Int

    fun inject(target: AppChangeLoggerService)

}

