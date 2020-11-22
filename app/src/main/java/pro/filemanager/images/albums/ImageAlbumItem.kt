package pro.filemanager.images.albums

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import pro.filemanager.images.ImageItem
import java.io.File
import java.io.Serializable

@Parcelize
data class ImageAlbumItem(
        val data: String,
        val displayName: String = File(data).name,
        val containedImages: @RawValue MutableList<ImageItem> = mutableListOf()
    ) : Parcelable, Serializable