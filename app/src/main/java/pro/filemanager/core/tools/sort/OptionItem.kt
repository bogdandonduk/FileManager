package pro.filemanager.core.tools.sort

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class OptionItem(val title: String, var action: @RawValue Runnable) : Parcelable