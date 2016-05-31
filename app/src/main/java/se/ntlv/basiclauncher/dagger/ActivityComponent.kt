package se.ntlv.basiclauncher.dagger

import android.app.Activity
import android.content.Context
import dagger.Component
import se.ntlv.basiclauncher.BasicLauncherApplication
import se.ntlv.basiclauncher.MainActivity

@ActivityScope
@Component(
        dependencies = arrayOf(ApplicationComponent::class),
        modules = arrayOf(
                ActivityModule::class,
                se.ntlv.basiclauncher.appgrid.Module::class,
                se.ntlv.basiclauncher.database.Module::class,
                se.ntlv.basiclauncher.image.Module::class
        )
)
interface ActivityComponent {

    companion object {
        fun init(module: ActivityModule): ActivityComponent = DaggerActivityComponent.builder()
                .activityModule(module)
                .applicationComponent(BasicLauncherApplication.graph)
                .build()
    }

    fun activity(): Activity

    fun context(): Context

    fun inject(main: MainActivity)
}

