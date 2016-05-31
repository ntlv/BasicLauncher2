package se.ntlv.basiclauncher.appgrid

import android.content.Context
import dagger.Module
import dagger.Provides
import se.ntlv.basiclauncher.database.AppDetailRepository
import se.ntlv.basiclauncher.image.ImageLoaderCacher

@Module
class Module() {

    @Provides
    fun provideGridFactory(context: Context,
                           clickHandler: AppIconClickHandler,
                           imageLoaderCacher: ImageLoaderCacher,
                           repo: AppDetailRepository): AppGridFactory =
            AppGridFactory(context, repo, clickHandler, imageLoaderCacher)

    @Provides
    fun provideAppDetailLayoutFactory(gridFactory: AppGridFactory): AppPageLayoutFactory =
            AppPageLayoutFactory(gridFactory)

    @Provides
    fun providesDoubleTapMenuHandler(context: Context,
                                     repository: AppDetailRepository): DoubleTapMenuHandler =
            DoubleTapMenuHandler(context, repository)
}
