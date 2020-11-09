package pro.filemanager.images

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.LruCache
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.MediaStoreSignature
import pro.filemanager.ApplicationLoader
import pro.filemanager.R

class ImageManager() {
    private var cursor: Cursor = ApplicationLoader.context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_MODIFIED,
    ), null, null, null)!!

    fun fetch() : MutableList<ImageItem> {
        val items: MutableList<ImageItem> = mutableListOf()

        if(cursor.moveToFirst()) {
            while(!cursor.isAfterLast) {
                items.add(ImageItem(
                        cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED))
                ))

                cursor.moveToNext()
            }
        }

        return items
    }

    fun closeCursor() {
        cursor.close()
    }

    companion object {
        const val MIME_TYPE = "image/*"

        val glideImageRequestBuilder = Glide.with(ApplicationLoader.context)
                .asBitmap()
                .load(R.drawable.placeholder_image_video_item_empty)
                .placeholder(R.drawable.placeholder_image_video_item)
                .thumbnail(0.5f)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()

        var preloadedImages: MutableList<ImageItem>? = null
        var preloadingInProgress = false

        @SuppressLint("NewApi")
        fun preloadImages(context: Context) {
            if((Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) || (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                preloadingInProgress = true

                val imageManager = ImageManager()

                preloadedImages = imageManager.fetch()

                imageManager.closeCursor()

                preloadingInProgress = false

            }
        }

    }

}