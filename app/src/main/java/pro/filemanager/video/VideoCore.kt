package pro.filemanager.video

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import pro.filemanager.ApplicationLoader
import pro.filemanager.R

object VideoCore {

    const val MIME_TYPE = "video/*"

    val glideRequestBuilder = Glide.with(ApplicationLoader.appContext)
            .asBitmap()
            .placeholder(R.drawable.placeholder_image_video_item)
            .thumbnail(0.5f)
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .centerCrop()

    val glideSimpleRequestBuilder = Glide.with(ApplicationLoader.appContext)
            .asBitmap()
            .centerCrop()
}