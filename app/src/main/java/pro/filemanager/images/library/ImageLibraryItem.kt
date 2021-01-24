package pro.filemanager.images.library

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import pro.filemanager.core.base.BaseLibraryItem
import java.io.Serializable

@Parcelize
data class ImageLibraryItem(
        override var data: String,
        override var displayName: String,
        override var size: Long,
        override var dateModified: Long,
        val width: Int,
        val height: Int
) : BaseLibraryItem(
        data,
        displayName,
        size,
        dateModified,
), Parcelable, Serializable