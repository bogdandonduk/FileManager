package pro.filemanager.images

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import pro.filemanager.ApplicationLoader
import pro.filemanager.R

object ImageCore {

    const val MIME_TYPE = "image/*"

    val glideRequestBuilder = Glide.with(ApplicationLoader.appContext)
        .asBitmap()
        .placeholder(R.drawable.placeholder_image_video_item)
        .thumbnail(0.5f)
        .dontAnimate()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .centerCrop()

    val glideSimpleRequestBuilder = Glide.with(ApplicationLoader.appContext)
        .asBitmap()
        .centerCrop()
}