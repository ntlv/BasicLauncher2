package se.ntlv.basiclauncher.database

import android.util.Log

class ImageLoaderErrorHandler(private val repo: AppDetailDB) {

    fun handleNameNotFound(ex: Throwable, packageName: String) {
        Log.e("ImageLoaderErrorHandler", "Exception during image loading: {$ex}")
        repo.updatePackage(packageName, ignore = true)
    }
}
