package pro.filemanager.core.base

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
open class BaseItem(
        open val data: String,
        open val displayName: String,
        open val size: Long,
        open val dateModified: Long,
        open val dateAdded: Long
) : Parcelable