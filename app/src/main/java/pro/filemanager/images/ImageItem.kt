package pro.filemanager.images

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import pro.filemanager.core.generics.BaseItem
import java.io.Serializable

@Parcelize
data class ImageItem(
        override var data: String,
        override var displayName: String,
        override var size: Long,
        override var dateModified: Long,
        val width: Int,
        val height: Int
) : BaseItem(
        data,
        displayName,
        size,
        dateModified,
), Parcelable, Serializable