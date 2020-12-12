package pro.filemanager.images

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import pro.filemanager.core.base.BaseItem
import java.io.Serializable

@Parcelize
data class ImageItem(
        override val data: String,
        override val displayName: String,
        override val size: Long,
        override val dateModified: Long,
        override val dateAdded: Long
) : BaseItem(
        data,
        displayName,
        size,
        dateModified,
        dateAdded
), Parcelable, Serializable