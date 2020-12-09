package pro.filemanager.images

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pro.filemanager.ApplicationLoader
import pro.filemanager.R
import pro.filemanager.core.base.BaseViewModel
import pro.filemanager.core.tools.sort.OptionItem
import pro.filemanager.core.tools.sort.SortBottomModalSheetFragment
import pro.filemanager.core.tools.sort.SortTool

object ImageCore {

    const val MIME_TYPE = "image/*"
    const val KEY_ARGUMENT_ALBUM_PARCELABLE = "chosenAlbum"

    val glideBitmapRequestBuilder = Glide.with(ApplicationLoader.appContext)
        .asBitmap()
        .placeholder(R.drawable.placeholder_image_video_item)
        .error(R.drawable.bg_glide_error)
        .thumbnail(0.5f)
        .dontAnimate()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .centerCrop()

    val glideGifRequestBuilder = Glide.with(ApplicationLoader.appContext)
        .asGif()
        .placeholder(R.drawable.placeholder_image_video_item)
        .error(R.drawable.bg_glide_error)
        .thumbnail(0.5f)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .centerCrop()

    val glideSimpleRequestBuilder = Glide.with(ApplicationLoader.appContext)
        .asBitmap()
        .centerCrop()
}