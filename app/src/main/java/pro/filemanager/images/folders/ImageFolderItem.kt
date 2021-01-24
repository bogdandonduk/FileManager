package pro.filemanager.images.folders

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import pro.filemanager.core.base.BaseFolderItem
import pro.filemanager.images.library.ImageLibraryItem
import java.io.File
import java.io.Serializable

@Parcelize
data class ImageFolderItem(
        override var data: String,
        override var displayName: String = File(data).name,
        var containedImages: MutableList<ImageLibraryItem> = mutableListOf(),
        override var totalSize: Long = 0
) : BaseFolderItem(
        data,
        displayName,
        containedImages,
        totalSize
), Parcelable, Serializable