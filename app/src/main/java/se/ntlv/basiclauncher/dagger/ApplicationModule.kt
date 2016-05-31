package se.ntlv.basiclauncher.dagger

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import com.google.android.gms.gcm.GcmNetworkManager
import dagger.Module
import dagger.Provides
import org.jetbrains.anko.defaultSharedPreferences
import se.ntlv.basiclauncher.database.AppDetailDB
import se.ntlv.basiclauncher.database.GlobalConfig
import java.io.File
import javax.inject.Named
import javax.inject.Singleton

@Module
class ApplicationModule(private val app: Application) {

    @Provides
    fun provideApplication(): Application = app

    @Provides
    fun provideContext(): Context = app

    @Provides
    fun providePackageManager(): PackageManager = app.packageManager

    @Singleton
    @Provides
    fun provideDatabase(): AppDetailDB = AppDetailDB(app)

    @Provides
    @Named("cache")
    fun providesCacheDir(): File = app.cacheDir

    @Provides
    @Named("version")
    fun providesAppVersion(): Int = app.packageManager.getPackageInfo(app.packageName, 0).versionCode

    @Provides
    fun provideGcmManager() : GcmNetworkManager = GcmNetworkManager.getInstance(app)

    @Provides
    fun provideConfig() : GlobalConfig = GlobalConfig(app.defaultSharedPreferences)
}
