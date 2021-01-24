package pro.filemanager.core.wrappers

import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import pro.filemanager.ApplicationLoader
import pro.filemanager.R

object GlideWrapper {
    val bitmapRequestBuilder = Glide.with(ApplicationLoader.appContext)
            .asBitmap()
            .placeholder(R.drawable.placeholder_library_item)
            .error(R.drawable.bg_glide_error)
            .thumbnail(0.5f)
            .priority(Priority.HIGH)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .centerCrop()


    val gifRequestBuilder = Glide.with(ApplicationLoader.appContext)
            .asGif()
            .placeholder(R.drawable.placeholder_library_item)
            .error(R.drawable.bg_glide_error)
            .thumbnail(0.5f)
            .priority(Priority.HIGH)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .centerCrop()

    val simpleRequestBuilder = Glide.with(ApplicationLoader.appContext)
            .asBitmap()
            .centerCrop()
}