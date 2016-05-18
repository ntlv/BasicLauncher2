package se.ntlv.basiclauncher.dagger

import android.app.Application
import dagger.Module
import dagger.Provides
import org.jetbrains.anko.defaultSharedPreferences
import se.ntlv.basiclauncher.repository.AppDetailDB
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Module
class ApplicationModule(private val app : Application) {

    @Provides
    @Singleton
    fun provideApplication() = app

    @Provides
    @Singleton
    fun provideAppDetailDb() = AppDetailDB(app, app.packageName)

    @Provides
    @Named("appName")
    fun providesAppName() = app.packageName

    @Provides
    @Singleton
    fun providePreferences() = app.defaultSharedPreferences

    @Provides
    @Singleton
    fun providePackageManager() = app.packageManager
}
