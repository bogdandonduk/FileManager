package pro.filemanager.core.generics

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import pro.filemanager.images.ImageItem
import java.io.File
import java.io.Serializable

@Parcelize
open class BaseFolderItem(
        open val data: String,
        open var displayName: String = File(data).name,
        open var containedItems: MutableList<out BaseItem> = mutableListOf(),
        open var totalSize: Long = 0
) : Parcelable, Serializable {
    override fun equals(other: Any?): Boolean =
            other != null &&
                    this::javaClass == other.javaClass &&
                    this.data == (other as BaseFolderItem).data &&
                    this.displayName == other.displayName &&
                    this.containedItems.first().data == other.containedItems.first().data

    override fun hashCode(): Int {
        var result = data.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + containedItems.hashCode()
        result = 31 * result + totalSize.hashCode()
        return result
    }
}