package pro.filemanager.core.base

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
open class BaseLibraryItem(
        override var data: String,
        override var displayName: String,
        open var size: Long,
        open var dateModified: Long
) : BaseItem(
        data,
        displayName
), Parcelable {
    override fun equals(other: Any?): Boolean =
            other != null &&
                    this::javaClass == other.javaClass &&
                    this.data == (other as BaseLibraryItem).data &&
                    this.displayName == other.displayName &&
                    this.size == other.size &&
                    this.dateModified == other.dateModified

    override fun hashCode(): Int {
        var result = data.hashCode()
            result = 31 * result + displayName.hashCode()
            result = 31 * result + size.hashCode()
            result = 31 * result + dateModified.hashCode()
        return result
    }
}