package pro.filemanager.core.base

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Parcelable
import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.core.tools.sort.SortTool

@Parcelize
open class BaseViewModel : ViewModel(), Parcelable {

    open var iOScope = CoroutineScope(IO)
    open var mainScope = CoroutineScope(Main)
    open var mainImmediateScope = CoroutineScope(Main.immediate)

    // must-override
    var currentSortOrder: String = SortTool.SORT_ORDER_DATE_RECENT
    //

    open var selectionTool = SelectionTool()

    @Volatile open var shouldScrollToTop = false
    @Volatile open var searchInProgress = false

    open var contentObserver: BaseContentObserver? = null

    open var mainListRvState: Parcelable? = null
    open var currentSearchQuery = ""

    val shownDialogs = mutableMapOf<String, Dialog>()

    @CallSuper
    open fun setSortOrder(context: Context, sortOrder: String, isPersistable: Boolean) {
        currentSortOrder = sortOrder
    }

    fun setSearchQuery(query: String?) {
        currentSearchQuery = query ?: ""
    }

    fun resetCoroutineScopeMain() {
        mainScope.cancel()
        mainImmediateScope.cancel()

        mainImmediateScope = CoroutineScope(Main.immediate)
        mainScope = CoroutineScope(Main)
    }

    fun resetFlags() {
        shouldScrollToTop = false
        searchInProgress = false
    }

    fun initContentObserver(context: Context, uri: Uri, handler: Handler) {
        if(contentObserver == null) contentObserver = BaseContentObserver(context, this, handler)

        context.contentResolver.registerContentObserver(uri, true, contentObserver!!)
    }

    fun releaseContentObserver(context: Context) {
        if(contentObserver != null)
            context.contentResolver.unregisterContentObserver(contentObserver!!)
    }

    open suspend fun initItemsLive(context: Context) { }

    open suspend fun assignItemsLive(context: Context, forceLoad: Boolean) { }
}