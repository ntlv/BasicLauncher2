package se.ntlv.basiclauncher.database

import com.google.android.gms.gcm.GcmNetworkManager
import dagger.Module
import dagger.Provides


@Module
class Module() {

    @Provides
    fun provideRepo(db: AppDetailDB): AppDetailRepository = AppDetailRepository(db)

    @Provides
    fun provideScheduler(manager: GcmNetworkManager): DbMaintenanceScheduler = DbMaintenanceScheduler(manager)
}
