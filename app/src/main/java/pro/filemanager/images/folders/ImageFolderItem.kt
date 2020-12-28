package pro.filemanager.images.folders

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import pro.filemanager.core.generics.BaseFolderItem
import pro.filemanager.images.ImageItem
import java.io.File
import java.io.Serializable

@Parcelize
data class ImageFolderItem(
        override val data: String,
        override val displayName: String = File(data).name,
        var containedImages: MutableList<ImageItem> = mutableListOf(),
        override var totalSize: Long = 0
) : BaseFolderItem(
        data,
        displayName,
        containedImages,
        totalSize
), Parcelable, Serializable