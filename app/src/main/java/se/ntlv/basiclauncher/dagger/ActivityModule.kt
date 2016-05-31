package se.ntlv.basiclauncher.dagger

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import dagger.Module
import dagger.Provides
import se.ntlv.basiclauncher.appgrid.AppIconClickHandler
import se.ntlv.basiclauncher.database.AppDetailRepository

@Module
class ActivityModule(private val base: Activity) {

    @Provides
    fun providesActivity(): Activity = base

    @Provides
    fun providesContext(): Context = base

    @Provides
    fun provideClickHandler(packageManager: PackageManager,
                            activity: Activity,
                            repository: AppDetailRepository): AppIconClickHandler =
            AppIconClickHandler(packageManager, activity, repository)

}
