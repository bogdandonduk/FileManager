package pro.filemanager.core.base

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.File
import java.io.Serializable

@Parcelize
open class BaseFolderItem(
        override var data: String,
        override var displayName: String = File(data).name,
        open var containedLibraryItems: MutableList<out BaseLibraryItem> = mutableListOf(),
        open var totalSize: Long = 0
) : BaseItem(
        data,
        displayName
), Parcelable, Serializable {
    override fun equals(other: Any?): Boolean =
            other != null &&
                    this::javaClass == other.javaClass &&
                    this.data == (other as BaseFolderItem).data &&
                    this.displayName == other.displayName &&
                    this.containedLibraryItems.first().data == other.containedLibraryItems.first().data

    override fun hashCode(): Int {
        var result = data.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + containedLibraryItems.hashCode()
        result = 31 * result + totalSize.hashCode()
        return result
    }
}