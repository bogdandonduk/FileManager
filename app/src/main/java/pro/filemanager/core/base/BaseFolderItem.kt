package pro.filemanager.core.base

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import pro.filemanager.images.ImageItem
import java.io.File
import java.io.Serializable

@Parcelize
open class BaseFolderItem(
        open val data: String,
        open val displayName: String = File(data).name,
        open var containedItems: MutableList<out BaseItem> = mutableListOf(),
        open var totalSize: Long = 0
) : Parcelable, Serializable {
    override fun equals(other: Any?): Boolean =
            other != null &&
                    this::javaClass == other.javaClass &&
                    this.data == (other as BaseItem).data &&
                    this.displayName == other.displayName &&
                    this.containedItems == other
}