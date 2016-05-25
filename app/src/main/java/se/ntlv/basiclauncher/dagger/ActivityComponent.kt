package se.ntlv.basiclauncher.dagger

import android.app.Activity
import android.content.Context
import dagger.Component
import se.ntlv.basiclauncher.MainActivity

@ActivityScope
@Component(dependencies = arrayOf(ApplicationComponent::class), modules = arrayOf(ActivityModule::class))
interface ActivityComponent {

    object Init {
        fun init(module: ActivityModule): ActivityComponent {
            return DaggerActivityComponent.builder()
                    .activityModule(module)
                    .applicationComponent(se.ntlv.basiclauncher.BasicLauncherApplication.graph)
                    .build()
        }
    }

    fun activity(): Activity

    fun context(): Context

    fun inject(main: MainActivity)
}

