package pro.filemanager.core.base

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
open class BaseItem(
        open var data: String,
        open var displayName: String,
        open var size: Long,
        open var dateModified: Long,
        open var dateAdded: Long
) : Parcelable {
    override fun equals(other: Any?): Boolean =
            other != null &&
                    this::javaClass == other.javaClass &&
                    this.data == (other as BaseItem).data &&
                    this.displayName == other.displayName &&
                    this.size == other.size &&
                    this.dateAdded == other.dateAdded &&
                    this.dateModified == other.dateModified

    override fun hashCode(): Int {
        var result = data.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + dateModified.hashCode()
        result = 31 * result + dateAdded.hashCode()
        return result
    }
}