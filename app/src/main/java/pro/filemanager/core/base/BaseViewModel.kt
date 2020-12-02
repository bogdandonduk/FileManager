package pro.filemanager.core.base

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.ViewModel
import kotlinx.android.parcel.Parcelize

@Parcelize
open class BaseViewModel : ViewModel(), Parcelable {
    open fun sortBySizeMax(context: Context) { }

    open fun sortBySizeMin(context: Context) { }

    open fun sortByDateRecent(context: Context) { }

    open fun sortByDateOldest(context: Context) { }

    open fun sortByNameAlphabetic(context: Context) { }

    open fun sortByNameReversed(context: Context) { }
}