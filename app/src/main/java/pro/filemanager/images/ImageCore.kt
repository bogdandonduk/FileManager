package pro.filemanager.images

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import pro.filemanager.ApplicationLoader
import pro.filemanager.R
import pro.filemanager.core.base.BaseViewModel
import pro.filemanager.core.tools.sort.OptionItem
import pro.filemanager.core.tools.sort.SortBottomModalSheetFragment
import pro.filemanager.core.tools.sort.SortTool

object ImageCore {

    const val MIME_TYPE = "image/*"
    const val KEY_ARGUMENT_ALBUM_PARCELABLE = "chosenAlbum"

    fun getImageItemSortOptions(context: Context, viewModel: BaseViewModel, bottomModalSheetFragment: SortBottomModalSheetFragment) : MutableList<OptionItem> {
        return mutableListOf<OptionItem>().apply {
            add(
                OptionItem("By Date (Recent)") {
                    viewModel.sortByDateRecent(context)
                    if(bottomModalSheetFragment.showsDialog)
                        bottomModalSheetFragment.dismiss()
                }
            )
            add(
                OptionItem("By Date (Oldest)") {
                    viewModel.sortByDateOldest(context)
                    if(bottomModalSheetFragment.showsDialog)
                        bottomModalSheetFragment.dismiss()
                }
            )
            add(
                OptionItem("By Name (Alphabet)") {
                    viewModel.sortByNameAlphabetic(context)
                    if(bottomModalSheetFragment.showsDialog)
                        bottomModalSheetFragment.dismiss()
                }
            )
            add(
                OptionItem("By Name (Reversed)") {
                    viewModel.sortByNameReversed(context)
                    if(bottomModalSheetFragment.showsDialog)
                        bottomModalSheetFragment.dismiss()
                }
            )
            add(
                OptionItem("By Size (Biggest)") {
                    viewModel.sortBySizeMax(context)
                    if(bottomModalSheetFragment.showsDialog)
                        bottomModalSheetFragment.dismiss()
                }
            )
            add(
                OptionItem("By Size (Smallest)") {
                    viewModel.sortBySizeMin(context)
                    if(bottomModalSheetFragment.showsDialog)
                        bottomModalSheetFragment.dismiss()
                }
            )

        }
    }

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