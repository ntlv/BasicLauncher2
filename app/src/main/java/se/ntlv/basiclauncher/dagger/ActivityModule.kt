package se.ntlv.basiclauncher.dagger

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import dagger.Module
import dagger.Provides
import se.ntlv.basiclauncher.appgrid.AppCellClickHandlerImpl
import se.ntlv.basiclauncher.appgrid.AppDetailLayoutFactory
import se.ntlv.basiclauncher.appgrid.AppGridFactory
import se.ntlv.basiclauncher.appgrid.DoubleTapMenuHandler
import se.ntlv.basiclauncher.image.ImageLoaderCacher
import se.ntlv.basiclauncher.repository.AppDetailDB
import se.ntlv.basiclauncher.repository.AppDetailRepository
import se.ntlv.basiclauncher.repository.ImageLoaderErrorHandler
import java.io.File
import javax.inject.Named

@ActivityScope
@Module
class ActivityModule(private val base: Activity) {

    @Provides
    @ActivityScope
    fun providesActivity(): Activity = base

    @Provides
    @ActivityScope
    fun providesContext(): Context = base

    @Provides
    fun providePreferences(): SharedPreferences = base.getPreferences(Context.MODE_PRIVATE)


    @Provides
    fun provideRepo(db: AppDetailDB): AppDetailRepository = AppDetailRepository(db)

    @Provides
    fun provideClickHandler(packageManager: PackageManager,
                            activity: Activity,
                            repository: AppDetailRepository): AppCellClickHandlerImpl =
            AppCellClickHandlerImpl(packageManager, activity, repository)

    @Provides
    fun provideGridFactory(context: Context,
                           clickHandler: AppCellClickHandlerImpl,
                           imageLoaderCacher: ImageLoaderCacher): AppGridFactory =
            AppGridFactory(context, clickHandler, imageLoaderCacher)

    @Provides
    fun provideAppDetailLayoutFactory(gridFactory: AppGridFactory): AppDetailLayoutFactory =
            AppDetailLayoutFactory(gridFactory)

    @Provides
    fun providesDoubleTapMenuHandler(context: Context, repository: AppDetailRepository): DoubleTapMenuHandler = DoubleTapMenuHandler(context, repository)

    @Provides
    fun providesImageLoaderErrorHandler(repo: AppDetailDB) = ImageLoaderErrorHandler(repo)

    @Provides
    fun providesCache(packageManager: PackageManager,
                      @Named("cache") cacheDir: File,
                      @Named("version") version: Int,
                      errorHandler: ImageLoaderErrorHandler): ImageLoaderCacher =
            ImageLoaderCacher(packageManager, cacheDir, version, errorHandler, base.resources)
}
