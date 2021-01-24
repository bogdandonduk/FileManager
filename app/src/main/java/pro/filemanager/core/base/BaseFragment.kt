package pro.filemanager.core.base

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcelable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pro.filemanager.ApplicationLoader
import pro.filemanager.R
import pro.filemanager.core.tools.toolbar.ToolbarAdapter
import pro.filemanager.core.tools.toolbar.ToolbarItem
import pro.filemanager.home.HomeActivity
import kotlin.math.abs

abstract class BaseFragment : Fragment() {

    open lateinit var frContext: Context
    open lateinit var activity: HomeActivity

    var translucentStatusBar = false
    var tabsBarVisible = true
    var toolBarVisible = true

    open lateinit var onBackCallback: OnBackPressedCallback
    open lateinit var searchBackCallback: OnBackPressedCallback

    open lateinit var searchTextWatcher: TextWatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = requireActivity() as HomeActivity

        frContext =
                try {
                    requireContext()
                } catch(thr: Throwable) {
                    activity.applicationContext
                } finally {
                    ApplicationLoader.appContext
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }

    fun isSearchBackCallbackInitialized() : Boolean = this::searchBackCallback.isInitialized

    fun isSearchTextWatcherInitialized() : Boolean = this::searchTextWatcher.isInitialized

    abstract fun launchCore()

    fun hideKeyboard(context: Context, root: View, flags: Int = 0) {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(root.windowToken, flags)
    }

    fun initClickableLayouts(clickableLayouts: MutableList<ViewGroup>) {
        clickableLayouts.forEach {
            it.setOnClickListener { }
        }
    }

    fun initAppBar(context: Context, toolbar: Toolbar, title: String?, navIconRes: Int?, navIconDesc: String? = null, navAction: (() -> Unit)? = null) {
        activity.setSupportActionBar(toolbar)

        setHasOptionsMenu(true)

        activity.supportActionBar!!.setDisplayShowTitleEnabled(title != null)
        activity.supportActionBar!!.title = title

        if(navIconRes != null) {
            toolbar.navigationIcon = ResourcesCompat.getDrawable(context.resources, navIconRes, null)

            if(navIconDesc != null) toolbar.navigationContentDescription = navIconDesc

            if(navAction != null) toolbar.setNavigationOnClickListener {
                navAction.invoke()
            }
        } else {
            activity.supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        }

        toolbar.overflowIcon = ResourcesCompat.getDrawable(frContext.resources, R.drawable.ic_baseline_overflow_menu_24, null)
    }

    fun initSearchBar(searchBar: EditText, hint: String, textWatcher: TextWatcher) {
        searchBar.hint = hint

        searchBar.addTextChangedListener(textWatcher)
    }

    open fun fetchOldSearchBarText() { }

    fun initListGridResizeBtn(
            activity: HomeActivity,
            resizeBtnLayout: ViewGroup,
            resizeBtnText: TextView,
            list: RecyclerView,
            adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
            viewModel: BaseViewModel,
            newSpanCountFinder: () -> Int,
    ) {
        resizeBtnLayout.setOnClickListener {
            val newSpanCount = newSpanCountFinder.invoke()

            setGridResizeBtnText(activity, resizeBtnText, newSpanCount)

            viewModel.mainImmediateScope.launch {
                initListGridLayoutManager(list, newSpanCount, null, null) {
                    viewModel.selectionTool.updateAll(adapter)
                }
            }
        }
    }

    open fun setGridResizeBtnText(activity: HomeActivity, resizeBtnText: TextView, currentSpanCount: Int) { }

    fun initSearchClearBtn(clearBtn: MenuItem, query: String, clickListener: MenuItem.OnMenuItemClickListener? = null) {
        if(clickListener != null) clearBtn.setOnMenuItemClickListener(clickListener)

        clearBtn.isVisible = query.isNotEmpty()
    }

    open fun restoreLastDialog() {

    }

    suspend fun initListGridLayoutManager(
            list: RecyclerView,
            spanCount: Int,
            state: Parcelable?,
            delay: Long?,
            afterAction: (() -> Unit)? = null) {
        if(list.layoutManager == null) {
            list.layoutManager = GridLayoutManager(context, spanCount)
        } else {
            (list.layoutManager as GridLayoutManager).spanCount = spanCount
        }

        state.run {
            if(this != null) list.layoutManager!!.onRestoreInstanceState(this)
        }

        if(delay != null) delay(delay)
        afterAction?.invoke()
    }

    fun initListAnimator(list: RecyclerView) {
        list.itemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean = true
        }

        (list.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    fun initListScrolling(list: RecyclerView, scrollingDownAction: () -> Unit, scrollingUpAction: () -> Unit) {
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dx > 0 || dy > 0) {
                    scrollingDownAction.invoke()
                } else {
                    scrollingUpAction.invoke()
                }
            }
        })
    }

    open fun notifyListEmpty(adapterItemCount: Int, text: TextView, gridResizeBtnLayout: ViewGroup, scrollBtnLayout: ViewGroup, list: RecyclerView) {
        return if(adapterItemCount > 0) {
            text.visibility = View.GONE
            gridResizeBtnLayout.visibility = View.VISIBLE
            scrollBtnLayout.visibility = View.VISIBLE
            list.isNestedScrollingEnabled = true
        } else {
            text.visibility = View.VISIBLE
            gridResizeBtnLayout.visibility = View.GONE
            scrollBtnLayout.visibility = View.GONE
            list.isNestedScrollingEnabled = false
        }
    }

    open fun <T : BaseItem> refreshListAdapter(adapter: ListAdapter<T, RecyclerView.ViewHolder>, items: MutableList<T>, force: Boolean) {
        if(force) adapter.submitList(null)
        adapter.submitList(items)
    }

    fun initAppBarLayoutCollapsing(appBarLayout: AppBarLayout, expandingAction: () -> Unit, collapsingAction: () -> Unit) {
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarL, verticalOffset ->
            if(abs(verticalOffset) == appBarL.height && !translucentStatusBar) {
                collapsingAction.invoke()
            } else if(verticalOffset == 0 && !translucentStatusBar){
                expandingAction.invoke()
            }
        })
    }

    fun initOnBackCallback(activity: AppCompatActivity, callback: OnBackPressedCallback) : OnBackPressedCallback =
            callback.apply { activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, this) }

    open fun initTabsBar(
            tabsBarLayout: ViewGroup,
            highlightTitle: TextView,
            indicator: ViewGroup,
            libraryLayout: ViewGroup,
            libraryTitle: TextView,
            libraryText: String,
            libraryOnClickListener: View.OnClickListener,
            foldersLayout: ViewGroup,
            foldersTitle: TextView,
            foldersOnClickListener: View.OnClickListener,
    ) {
        tabsBarLayout.post {
            libraryTitle.text = libraryText
            foldersTitle.text = resources.getText(R.string.title_folders)

            highlightTitle.setTypeface(null, Typeface.BOLD)
            indicator.visibility = View.VISIBLE
        }

        libraryLayout.setOnClickListener(libraryOnClickListener)
        foldersLayout.setOnClickListener(foldersOnClickListener)
    }

    open fun initSelectionBar(
            selectionBarLayout: ViewGroup,
            selectionCheckBox: CheckBox,
            selectionCheckBoxOnCheckedChangeListener: CompoundButton.OnCheckedChangeListener
    ) {
        initClickableLayouts(mutableListOf<ViewGroup>().apply { add(selectionBarLayout) })
        selectionCheckBox.setOnCheckedChangeListener(selectionCheckBoxOnCheckedChangeListener)
    }

    open fun initToolBar(
            context: Context,
            toolbarList: RecyclerView,
            toolbarItems: MutableList<ToolbarItem>,
            layoutInflater: LayoutInflater
    ) {
        toolbarList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        toolbarList.adapter = ToolbarAdapter(frContext, toolbarItems, layoutInflater)
    }

    open fun initBars(selectionMode: Boolean, tabsBar: ViewGroup, toolbar: ViewGroup) {
        if(!selectionMode) {
            tabsBar.visibility = View.VISIBLE
            toolbar.visibility = View.GONE
        } else {
            tabsBar.visibility = View.GONE
            toolbar.visibility = View.VISIBLE
        }
    }
}