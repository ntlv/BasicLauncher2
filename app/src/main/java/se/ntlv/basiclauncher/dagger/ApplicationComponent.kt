package se.ntlv.basiclauncher.dagger

import android.app.Application
import android.content.pm.PackageManager
import com.google.android.gms.gcm.GcmNetworkManager
import dagger.Component
import se.ntlv.basiclauncher.database.AppDetailDB
import se.ntlv.basiclauncher.database.GlobalConfig
import se.ntlv.basiclauncher.packagehandling.AppChangeLoggerService
import java.io.File
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {

    companion object {
        fun init(module: ApplicationModule): ApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(module)
                .build()
    }

    fun application(): Application

    fun db(): AppDetailDB

    fun packageManager(): PackageManager

    @Named("cache")
    fun cache(): File

    @Named("version")
    fun version(): Int

    fun networkManager() : GcmNetworkManager

    fun globalCOnfig() : GlobalConfig

    fun inject(target: AppChangeLoggerService)

}

