package se.ntlv.basiclauncher.dagger

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import dagger.Module
import dagger.Provides
import se.ntlv.basiclauncher.repository.AppDetailDB
import javax.inject.Named

@Module
class ApplicationModule(private val app: Application) {

    @Provides
    @ApplicationScope
    fun provideApplication() = app

    @Provides
    @ApplicationScope
    fun provideContext() : Context = app

    @Provides
    @ApplicationScope
    fun providePackageManager() : PackageManager = app.packageManager

    @Provides
    @ApplicationScope
    fun provideDatabase() : AppDetailDB = AppDetailDB(app, app.packageName)

//    @Provides
//    @ApplicationScope
//    fun providesImageLoaderErrorHandler(repo: AppDetailDB) = ImageLoaderErrorHandler(repo)

//    @Provides
//    fun provideImageLoader(pm : PackageManager, errorHandler : ImageLoaderErrorHandler) =
//            ImageLoader(pm, errorHandler)

    @Provides
    @Named("cache")
    fun providesCache() = app.cacheDir

    @Provides
    @Named("version")
    fun providesVersion() = app.packageManager.getPackageInfo(app.packageName, 0).versionCode


}
