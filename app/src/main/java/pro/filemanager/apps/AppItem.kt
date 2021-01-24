package pro.filemanager.apps

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import pro.filemanager.core.base.BaseLibraryItem
import java.io.Serializable

@Parcelize
data class AppItem(
        override var data: String,
        override var displayName: String,
        override var size: Long,
        var dateInstalled: Long
) : BaseLibraryItem(
        data,
        displayName,
        size,
        dateInstalled,
), Parcelable, Serializable