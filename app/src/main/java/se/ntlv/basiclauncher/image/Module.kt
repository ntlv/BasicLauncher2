package se.ntlv.basiclauncher.image

import android.app.Application
import android.content.pm.PackageManager
import dagger.Module
import dagger.Provides
import se.ntlv.basiclauncher.database.AppDetailDB
import se.ntlv.basiclauncher.database.ImageLoaderErrorHandler
import se.ntlv.basiclauncher.image.ImageLoaderCacher
import java.io.File
import javax.inject.Named

@Module
class Module {

    @Provides
    fun providesImageLoaderErrorHandler(repo: AppDetailDB) = ImageLoaderErrorHandler(repo)

    @Provides
    fun providesCache(packageManager: PackageManager,
                      @Named("cache") cacheDir: File,
                      @Named("version") version: Int,
                      app: Application,
                      errorHandler: ImageLoaderErrorHandler): ImageLoaderCacher =
            ImageLoaderCacher(packageManager, cacheDir, version, errorHandler, app.resources)
}