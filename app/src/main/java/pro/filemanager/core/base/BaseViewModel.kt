package pro.filemanager.core.base

import android.content.Context
import android.os.Parcelable
import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import kotlinx.android.parcel.Parcelize
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.core.tools.sort.SortTool

@Parcelize
open class BaseViewModel : ViewModel(), Parcelable {

    // must-override
    open var currentSortOrder: String = SortTool.SORT_ORDER_DATE_RECENT

    open var selectionTool = SelectionTool()

    @CallSuper
    open fun setSortOrder(context: Context, sortOrder: String, isPersistable: Boolean) {
        currentSortOrder = sortOrder
    }

    open suspend fun assignItemsLive(context: Context, forceLoad: Boolean) { }
}