package pro.filemanager.core.generics

import android.content.Context
import android.os.Parcelable
import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import pro.filemanager.core.wrappers.PermissionWrapper
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.core.tools.sort.SortTool
import java.lang.Runnable

@Parcelize
open class BaseViewModel : ViewModel(), Parcelable {

    var IOScope = CoroutineScope(IO)
    open var MainScope: CoroutineScope? = CoroutineScope(Main)

    // must-override
    open var currentSortOrder: String = SortTool.SORT_ORDER_DATE_RECENT

    open var selectionTool = SelectionTool()

    @Volatile open var shouldScrollToTop = false

    var contentObserver: BaseContentObserver? = null

    @CallSuper
    open fun setSortOrder(context: Context, sortOrder: String, isPersistable: Boolean) {
        currentSortOrder = sortOrder
    }

    open suspend fun assignItemsLive(context: Context, forceLoad: Boolean) { }
}