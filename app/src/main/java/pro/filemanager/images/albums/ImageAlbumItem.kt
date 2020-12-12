package pro.filemanager.images.albums

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import pro.filemanager.core.base.BaseAlbumItem
import pro.filemanager.images.ImageItem
import java.io.File
import java.io.Serializable

@Parcelize
data class ImageAlbumItem(
        override val data: String,
        override val displayName: String = File(data).name,
        var containedImages: MutableList<ImageItem> = mutableListOf(),
        override var totalSize: Long = 0
) : BaseAlbumItem(
        data,
        displayName,
        containedImages,
        totalSize
), Parcelable, Serializable