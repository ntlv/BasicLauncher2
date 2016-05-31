package se.ntlv.basiclauncher.image

import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.PNG
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.LruCache
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import org.jetbrains.anko.image
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import se.ntlv.basiclauncher.database.ImageLoaderErrorHandler
import se.ntlv.basiclauncher.tag
import java.io.*
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean


class ImageLoaderCacher : Closeable {

    private val CACHE_MAX_SIZE: Long = 1024 * 1024 * 10
    private val mCacheDir: File
    private val mVersion: Int

    private val TAG = tag()

    private val zipper =
            { i: String, c: Boolean, m: LruCache<String, Drawable>, d: DiskLruCache, r: Resources ->
                Request(identifier = i, isClosed = c, memCache = m, diskCache = d, resources = r)
            }

    override fun close() {
        if (isClosed.compareAndSet(false, true)) {
            Log.v(TAG, "CLOSING CACHE")
            mDiskCacheSource.toBlocking().single().close()
        } else {
            Log.e(TAG, "Attempted to close already closed cache.")
        }
    }

    private val isClosed = AtomicBoolean(false)

    private val mPackageManager: PackageManager

    private val mOnPackageNotFound: ImageLoaderErrorHandler

    private lateinit var mDiskCacheSource: Observable<DiskLruCache>
    private lateinit var mMemCacheSource: Observable<LruCache<String, Drawable>>

    private lateinit var mIsClosedSource: Observable<Boolean>

    private val mRes: Resources

    constructor(pm: PackageManager, cacheDir: File, version: Int, onPackageNotFound: ImageLoaderErrorHandler, res: Resources) {
        mPackageManager = pm
        mCacheDir = cacheDir
        mVersion = version
        mOnPackageNotFound = onPackageNotFound
        mRes = res
    }


    fun init() {
        initMemCache();
        initDiskCache(mCacheDir, mVersion);
    }

    private fun initMemCache() {
        val maxMemKilobyte = Runtime.getRuntime().maxMemory() / 1024
        val cacheSize = (maxMemKilobyte / 8).toInt()

        val memCache = LruCache<String, Drawable>(cacheSize)
        mMemCacheSource = Observable.just(memCache).cache()
    }

    data class DiskCacheInitOptions(val cacheDir: File,
                                    val version: Int,
                                    val valueCount: Int,
                                    val size: Long)

    private fun initDiskCache(cacheDir: File, appVersion: Int) {
        Log.d(TAG, "INIT DISK CACHE")

        val opts = DiskCacheInitOptions(cacheDir, appVersion, 1, CACHE_MAX_SIZE)

        //observable that emits a value to indicate whether the cache has been closed or not
        mIsClosedSource = Observable.create {
            it.onNext(isClosed.get())
            it.onCompleted()
        }

        //observable that creates a cache and then emits the same cache on every subscription
        mDiskCacheSource = Observable.just(opts)
                .subscribeOn(Schedulers.io())
                .map {
                    Log.d(TAG, "CREATING CACHE")
                    DiskLruCache.open(it.cacheDir, it.version, it.valueCount, it.size)
                }
                .cache()
    }

    //empty super class so that the rx stream below type checks
    private abstract class Either

    //a loaded image, ready to be used in an image view
    private class EitherImage(val drawable: Drawable) : Either()

    //something went sideways during image loading, pass the package name and the exception to error handler
    private class EitherError(val error: Throwable, val packageName: String) : Either()

    data class Request(val isClosed: Boolean,
                       val identifier: String,
                       val memCache: LruCache<String, Drawable>,
                       val diskCache: DiskLruCache,
                       val resources: Resources)

    fun loadImage(bitmapIdentifier: String, targetStrongRef: ImageView, width: Int, height: Int): Subscription {
        val target = WeakReference<ImageView>(targetStrongRef)

        val identifierSource = Observable.just(bitmapIdentifier)
        val resources = Observable.just(mRes)

        return Observable
                .zip(identifierSource, mIsClosedSource, mMemCacheSource, mDiskCacheSource, resources, zipper)
                .subscribeOn(Schedulers.io())
                .map { it: Request ->
                    if (it.isClosed) throw(IllegalStateException("Attempted read from closed cache"))
                    else try {
                        val img = loadFromMem(it) ?: loadFromDisk(it, width, height) ?: loadFromPackageManager(it)
                        EitherImage(img)
                    } catch (ex: PackageManager.NameNotFoundException) {
                        EitherError(ex, it.identifier)
                    }
                }
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result: Either? ->
                    when (result) {
                        is EitherError ->
                            mOnPackageNotFound.handleNameNotFound(result.error, result.packageName)
                        is EitherImage -> {
                            target.get()?.setDrawableWithFadeAnimation(result.drawable)
                        }
                    }
                }
    }

    fun ImageView.setDrawableWithFadeAnimation(drawable: Drawable) {
        val anim = AlphaAnimation(0f, 1f)
        anim.duration = 300
        image = drawable
        startAnimation(anim)
    }

    private fun loadFromPackageManager(r: ImageLoaderCacher.Request): Drawable {
        val drawable = mPackageManager.getApplicationIcon(r.identifier)

        val bitmap = drawable.toBitmap()
        writeDrawableToCacheAsBitmap(r.identifier, drawable, r.memCache)
        writeDrawableToCacheAsBitmap(r.identifier, bitmap, r.diskCache)

        return drawable
    }

    private fun writeDrawableToCacheAsBitmap(key: String, drawable: Drawable, cache: LruCache<String, Drawable>) {
        Log.v(TAG, "Writing key:{$key}, value:{$drawable} to cache:{$cache}")
        cache.put(key, drawable)
    }

    private fun writeDrawableToCacheAsBitmap(key: String, bitmap: Bitmap, cache: DiskLruCache) {
        val editor : DiskLruCache.Editor?
        try {
            editor = cache.edit(key)
        } catch (ex : IOException) {
            Log.e(TAG, "Exception when opening cache editor", ex)
            return
        }
        if (editor == null) {
            Log.e(TAG, "Unable to open cache editor")
            return
        }
        val outStream : OutputStream
        try {
            outStream = editor.newOutputStream(0)
        } catch (ex : IOException ) {
            Log.e(TAG, "Exception opening editor stream", ex)
            editor.abort()
            return
        } catch (ex : IllegalStateException) {
            Log.e(TAG, "Exception opening editor stream", ex)
            editor.abort()
            return
        }

        val writeOk = bitmap.compress(PNG, 100, outStream) //note, magic number 100 is ignored by PNG compression
        when (writeOk){
            true -> {
                Log.v(TAG, "Writing key:{$key}, value:{$bitmap} to cache:{$cache}")
                editor.commit() }
            false -> {
                Log.e(TAG, "Failed to write {$key, $bitmap} to stream $outStream")
                editor.abort()
            }
        }
        outStream.close()
    }


    private fun loadFromDisk(r: Request, reqWidth: Int, reqHeight: Int): Drawable? {
        val snapshot: DiskLruCache.Snapshot?
        try {
            snapshot = r.diskCache.get(r.identifier)
        } catch (ex: IOException) {
            snapshot = null
        }
        val stream = snapshot?.getInputStream(0) as? FileInputStream ?: return null

        val fd: FileDescriptor?
        try {
            fd = stream.fd
        } catch(ex: IOException) {
            stream.close()
            return null
        }

        val opts = BitmapFactory.Options()
        opts.inJustDecodeBounds = true
        BitmapFactory.decodeFileDescriptor(fd, null, opts)

        opts.inSampleSize = calculateSampleSize(opts.outHeight, opts.outWidth, reqHeight, reqWidth)
        opts.inJustDecodeBounds = false

        //calc sample size end
        val bitmap = BitmapFactory.decodeFileDescriptor(fd, null, opts)

        val res = when {
            bitmap != null -> BitmapDrawable(r.resources, bitmap)
            else -> null
        }

        Log.v(TAG, "$res read from disk")
        stream.close()

        return res
    }

    private fun loadFromMem(r: ImageLoaderCacher.Request): Drawable? {
        val i = r.identifier
        val target = r.memCache.get(i)
        val modifier = if (target != null) "" else "NOT"
        Log.v(TAG, "$i $modifier found in mem")
        return target
    }


    fun Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable && bitmap != null) {
            return bitmap
        }

        val height = intrinsicHeight
        val width = intrinsicWidth

        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        draw(canvas)

        return bitmap
    }

    fun calculateSampleSize(height: Int, width: Int, reqHeight: Int, reqWidth: Int): Int {
        var sampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / sampleSize) > reqHeight
                    && (halfWidth / sampleSize) > reqWidth) {

                sampleSize *= 2
            }

            var totalPixels = width * height / sampleSize
            val totalRequiredPixelsCap = reqWidth * reqHeight * 2

            while (totalPixels > totalRequiredPixelsCap) {
                sampleSize *= 2
                totalPixels /= 2
            }
        }
        return sampleSize
    }
}
