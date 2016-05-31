package se.ntlv.basiclauncher.database

import dagger.Component
import se.ntlv.basiclauncher.dagger.ApplicationComponent
import se.ntlv.basiclauncher.dagger.ApplicationScope


@ApplicationScope
@Component(dependencies = arrayOf(ApplicationComponent::class), modules = arrayOf(Module::class))
interface Component {

    fun database() : AppDetailRepository
}