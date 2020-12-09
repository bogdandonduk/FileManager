package pro.filemanager.core.base

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.android.parcel.Parcelize
import pro.filemanager.core.tools.sort.SortTool
import pro.filemanager.images.ImageItem

@Parcelize
open class BaseViewModel : ViewModel(), Parcelable {

    // must-override
    open var currentSortOrder: String = SortTool.SORT_ORDER_DATE_RECENT

    open fun setSortOrder(context: Context, sortOrder: String, isPersistable: Boolean) {
        currentSortOrder = sortOrder
    }

    open suspend fun assignItemsLive(context: Context) { }
}