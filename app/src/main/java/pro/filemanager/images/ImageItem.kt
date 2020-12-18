package pro.filemanager.images

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import pro.filemanager.core.base.BaseItem
import java.io.Serializable

@Parcelize
data class ImageItem(
        override var data: String,
        override var displayName: String,
        override var size: Long,
        override var dateModified: Long,
        override var dateAdded: Long,
        val width: Int,
        val height: Int
) : BaseItem(
        data,
        displayName,
        size,
        dateModified,
        dateAdded
), Parcelable, Serializable