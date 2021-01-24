package pro.filemanager.images.library

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import elytrondesign.lib.android.permissionwrapper.PermissionWrapper
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import pro.filemanager.R
import pro.filemanager.core.base.*
import pro.filemanager.core.tools.delete.DeleteTool
import pro.filemanager.core.tools.ShareTool
import pro.filemanager.core.tools.info.InfoTool
import pro.filemanager.core.tools.rename.RenameTool
import pro.filemanager.core.tools.sort.SortTool
import pro.filemanager.core.tools.toolbar.ToolbarItem
import pro.filemanager.core.ui.FragmentWrapper
import pro.filemanager.core.ui.UIManager
import pro.filemanager.core.wrappers.CoroutineWrapper
import pro.filemanager.core.wrappers.MimeTypeWrapper
import pro.filemanager.core.wrappers.PreferencesWrapper
import pro.filemanager.databinding.FragmentImageLibraryBinding
import pro.filemanager.files.FileCore
import pro.filemanager.home.HomeActivity
import pro.filemanager.images.ImageRepo
import pro.filemanager.images.folders.ImageFolderItem
import pro.filemanager.images.folders.ImageFoldersFragment

@Suppress("UNCHECKED_CAST")
class ImageLibraryFragment : BaseFragment(), Observer<MutableList<ImageLibraryItem>> {
    lateinit var viewModel: ImageLibraryViewModel
    lateinit var binding: FragmentImageLibraryBinding

    var folderItem: ImageFolderItem? = null

    private val mainListScrollDownAction = {
        if(isViewModelInitialized()) {
            viewModel.mainScope.launch {
                hideTabsBar.invoke()
                hideToolbar.invoke()

                scrollDownBtnInitializer.invoke()
            }
        }
    }

    val scrollDownBtnInitializer = {
        binding.fragmentImageLibraryScrollBtnInclude.layoutScrollBtnIcon.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)

        binding.fragmentImageLibraryScrollBtnInclude.layoutScrollBtnRootLayout.setOnClickListener {
            binding.fragmentImageLibraryList.scrollToPosition(if(binding.fragmentImageLibraryList.adapter!!.itemCount > 0) binding.fragmentImageLibraryList.adapter!!.itemCount - 1 else 0)
            hideTabsBar.invoke()
            hideToolbar.invoke()
        }
    }

    private val mainListScrollUpAction = {
        if(isViewModelInitialized()) {
            viewModel.mainScope.launch {
                showTabsBar.invoke()
                showToolbar.invoke()

                scrollUpBtnInitializer.invoke()
            }
        }
    }

    val scrollUpBtnInitializer = {
        binding.fragmentImageLibraryScrollBtnInclude.layoutScrollBtnIcon.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)

        binding.fragmentImageLibraryScrollBtnInclude.layoutScrollBtnRootLayout.setOnClickListener {
            binding.fragmentImageLibraryList.scrollToPosition(0)
        }
    }

    val showToolbar = {
        if(!toolBarVisible) {
            if(viewModel.selectionTool.selectionMode) {
                binding.fragmentImageLibraryBottomToolbarInclude.layoutBottomToolbarRootLayout.visibility = View.VISIBLE
                binding.fragmentImageLibraryBottomToolbarInclude.layoutBottomToolbarRootLayout.animate().alpha(1f).start()
            } else
                binding.fragmentImageLibraryBottomToolbarInclude.layoutBottomToolbarRootLayout.visibility = View.GONE

            toolBarVisible = true
        }
    }

    val hideToolbar = {
        if(toolBarVisible) {
            binding.fragmentImageLibraryBottomToolbarInclude.layoutBottomToolbarRootLayout.animate().alpha(0f).start()
            activity.handler.postDelayed(
                    {
                        binding.fragmentImageLibraryBottomToolbarInclude.layoutBottomToolbarRootLayout.visibility = View.GONE
                    }, 300
            )

            toolBarVisible = false
        }
    }

    val showTabsBar = {
        if(!tabsBarVisible) {
            if(!viewModel.selectionTool.selectionMode) {
                binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.VISIBLE
                binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout.animate().alpha(1f).start()
            } else
                binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.GONE

            tabsBarVisible = true
        }
    }

    val hideTabsBar = {
        if(tabsBarVisible) {
            binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout.animate().alpha(0f).start()
            activity.handler.postDelayed(
                    {
                        binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.GONE
                    }, 300
            )

            tabsBarVisible = false
        }

    }

    var infoAction = {
        if(viewModel.selectionTool.selectedPaths.isNotEmpty()) {
            InfoTool.showInfoForLibraryItems(
                activity.supportFragmentManager,
                mutableListOf<BaseLibraryItem>().apply {
                    (binding.fragmentImageLibraryList.adapter as ImageLibraryAdapter).currentList.forEach {
                        if (viewModel.selectionTool.selectedPaths.contains(it.data)) {
                            add(it)
                        }
                    }
                }
            )
        }
    }

    var shareAction = {
        if(viewModel.selectionTool.selectedPaths.isNotEmpty()) {
            CoroutineWrapper.globalIOScope.launch {
                ShareTool.shareItems(frContext, viewModel.selectionTool.selectedPaths, MimeTypeWrapper.IMAGE_GENERIC_MIME_TYPE, true)
            }
        }
    }

    var moveAction = {

    }

    var copyAction = {

    }

    var renameAction = {
        if(viewModel.selectionTool.selectedPaths.size == 1) {
            CoroutineWrapper.globalIOScope.launch {
                try {
                    RenameTool.showRenameBottomModalSheetFragment(activity.supportFragmentManager, viewModel.selectionTool.selectedPaths[0]) {
                        try {
                            CoroutineWrapper.globalIOScope.launch {
                                viewModel.assignItemsLive(frContext, true)
                                ImageRepo.getSingleton().loadAll(frContext, true)

                                viewModel.selectionTool.selectionMode = false

                                withContext(Main) {
                                    viewModel.selectionTool.initSelectionState(
                                        activity,
                                        binding.root,
                                        binding.fragmentImageLibraryList.adapter!!,
                                        binding.fragmentImageLibraryBottomToolbarInclude.layoutBottomToolbarRootLayout,
                                        binding.fragmentImageLibraryAppBarInclude.baseToolbar,
                                        binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                                        binding.fragmentImageLibrarySelectionBarInclude.layoutSelectionBarRootLayout,
                                        binding.fragmentImageLibrarySelectionBarInclude.layoutSelectionBarSelectionCountCb,
                                        viewModel.selectionTool.selectionMode,
                                        viewModel.selectionTool.selectedPaths.size
                                    )
                                }
                            }
                        } catch (thr: Throwable) {

                        }
                    }
                } catch (thr: Throwable) {

                }
            }
        }
    }

    var deleteAction = {
        if(viewModel.selectionTool.selectedPaths.isNotEmpty()) {
            try {
                DeleteTool.delete(activity, binding.fragmentImageLibraryRootLayout, viewModel.selectionTool.selectedPaths, viewModel.shownDialogs) {
                    CoroutineWrapper.globalIOScope.launch {
                        try {
                            viewModel.assignItemsLive(frContext, true)
                            ImageRepo.getSingleton().loadAll(frContext, true)

                            viewModel.selectionTool.selectionMode = false

                            withContext(Main) {
                                viewModel.selectionTool.initSelectionState(
                                    activity,
                                    binding.root,
                                    binding.fragmentImageLibraryList.adapter!!,
                                    binding.fragmentImageLibraryBottomToolbarInclude.layoutBottomToolbarRootLayout,
                                    binding.fragmentImageLibraryAppBarInclude.baseToolbar,
                                    binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                                    binding.fragmentImageLibrarySelectionBarInclude.layoutSelectionBarRootLayout,
                                    binding.fragmentImageLibrarySelectionBarInclude.layoutSelectionBarSelectionCountCb,
                                    viewModel.selectionTool.selectionMode,
                                    viewModel.selectionTool.selectedPaths.size
                                )
                            }
                        } catch (thr: Throwable) {

                        }
                    }
                }
            } catch (thr: Throwable) {

            }
        }
    }

    fun isViewModelInitialized() : Boolean = this::viewModel.isInitialized

    fun isBindingInitialized() : Boolean = this::binding.isInitialized

    override fun onChanged(newItems: MutableList<ImageLibraryItem>?) {
        if(isViewModelInitialized() && isBindingInitialized() && binding.fragmentImageLibraryList.adapter != null) {
            try {
                viewModel.resetCoroutineScopeMain()
            } catch(thr: Throwable) {

            }

            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            refreshListAdapter(binding.fragmentImageLibraryList.adapter as ListAdapter<ImageLibraryItem, RecyclerView.ViewHolder>, newItems!!, viewModel.shouldScrollToTop)

            viewModel.resetFlags()

            notifyListEmpty(
                    newItems.size,
                    binding.fragmentImageLibraryNoImagesTitle,
                    binding.fragmentImageLibraryGridResizeBtnInclude.layoutGridResizeBtnRootLayout,
                    binding.fragmentImageLibraryScrollBtnInclude.layoutScrollBtnRootLayout,
                    binding.fragmentImageLibraryList
            )

            viewModel.selectionTool.initSelectionState(
                    activity,
                    binding.root,
                    binding.fragmentImageLibraryList.adapter!!,
                    binding.fragmentImageLibraryBottomToolbarInclude.layoutBottomToolbarRootLayout,
                    binding.fragmentImageLibraryAppBarInclude.baseToolbar,
                    binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                    binding.fragmentImageLibrarySelectionBarInclude.layoutSelectionBarRootLayout,
                    binding.fragmentImageLibrarySelectionBarInclude.layoutSelectionBarSelectionCountCb,
                    viewModel.selectionTool.selectionMode,
                    viewModel.selectionTool.selectedPaths.size
            )
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageLibraryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            folderItem = arguments?.getParcelable(FileCore.KEY_ARGUMENT_FOLDER_PARCELABLE)

            initAppBar(frContext, binding.fragmentImageLibraryAppBarInclude.baseToolbar, null, R.drawable.ic_baseline_arrow_back_24, frContext.resources.getString(R.string.go_back)) {
                hideKeyboard(frContext, view, 0)
                activity.onBackPressed()
            }

            launchCore()
        } catch(thr: Throwable) {

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    suspend fun initList(imageItems: MutableList<ImageLibraryItem>) {
        initListGridLayoutManager(
                binding.fragmentImageLibraryList,
                UIManager.getImageLibraryGridSpanNumber(activity),
                viewModel.mainListRvState,
                null
        ) {
            viewModel.mainListRvState = null
        }

        binding.fragmentImageLibraryList.adapter = ImageLibraryAdapter(requireActivity(), imageItems, layoutInflater, this).apply {
            setHasStableIds(true)
        }

        initListAnimator(binding.fragmentImageLibraryList)

        initListGridResizeBtn(
                activity,
                binding.fragmentImageLibraryGridResizeBtnInclude.layoutGridResizeBtnRootLayout,
                binding.fragmentImageLibraryGridResizeBtnInclude.layoutGridResizeBtnTitle,
                binding.fragmentImageLibraryList,
                binding.fragmentImageLibraryList.adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>,
                viewModel
        ) {
            UIManager.resizeImageLibraryListGrid(activity)
        }

        setGridResizeBtnText(
                activity,
                binding.fragmentImageLibraryGridResizeBtnInclude.layoutGridResizeBtnTitle,
                (binding.fragmentImageLibraryList.layoutManager as GridLayoutManager).spanCount
        )

        initListScrolling(binding.fragmentImageLibraryList, mainListScrollDownAction, mainListScrollUpAction)

        notifyListEmpty(imageItems.size, binding.fragmentImageLibraryNoImagesTitle, binding.fragmentImageLibraryGridResizeBtnInclude.layoutGridResizeBtnRootLayout, binding.fragmentImageLibraryScrollBtnInclude.layoutScrollBtnRootLayout, binding.fragmentImageLibraryList)
    }

    override fun setGridResizeBtnText(activity: HomeActivity, resizeBtnText: TextView, currentSpanCount: Int) {
        if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            when(currentSpanCount) {
                4 -> {
                    resizeBtnText.text = (6).toString()
                }
                6 -> {
                    resizeBtnText.text = (8).toString()
                }
                8 -> {
                    resizeBtnText.text = (4).toString()
                }
                else -> {
                    resizeBtnText.text = (6).toString()
                }
            }
        } else {
            when(currentSpanCount) {
                3 -> {
                    resizeBtnText.text = (4).toString()
                }
                4 -> {
                    resizeBtnText.text = (5).toString()
                }
                5 -> {
                    resizeBtnText.text = (3).toString()
                }
                else -> {
                    resizeBtnText.text = (4).toString()
                }
            }
        }
    }

    override fun launchCore() {
        PermissionWrapper.requestStorageGroup(
                activity,
                binding.fragmentImageLibraryRootLayout
        ) {
            CoroutineWrapper.globalMainScope.launch {
                viewModel = ViewModelProvider(this@ImageLibraryFragment, ImageLibraryViewModelFactory(frContext, ImageRepo.getSingleton(), folderItem)).get(ImageLibraryViewModel::class.java)

                viewModel.getItemsLive(frContext).observe(viewLifecycleOwner, this@ImageLibraryFragment)

                initList(viewModel.getItemsLive(frContext).value!!)

                binding.fragmentImageLibraryListProgressBar.visibility = View.GONE

                initAppBarLayoutCollapsing(binding.fragmentImageLibraryAppBarLayout, {
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                }) {
                    activity.window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                }

                onBackCallback = initOnBackCallback(activity, object : OnBackPressedCallback(folderItem == null) {
                    override fun handleOnBackPressed() {
                        activity.supportFragmentManager.popBackStack()
                    }
                })

                viewModel.initContentObserver(
                        frContext,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        activity.handler
                )

                viewModel.selectionTool.initSelectionState(
                        activity,
                        binding.root,
                        binding.fragmentImageLibraryList.adapter!!,
                        binding.fragmentImageLibraryBottomToolbarInclude.layoutBottomToolbarRootLayout,
                        binding.fragmentImageLibraryAppBarInclude.baseToolbar,
                        binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                        binding.fragmentImageLibrarySelectionBarInclude.layoutSelectionBarRootLayout,
                        binding.fragmentImageLibrarySelectionBarInclude.layoutSelectionBarSelectionCountCb,
                        viewModel.selectionTool.selectionMode,
                        viewModel.selectionTool.selectedPaths.size
                )

                fetchOldSearchBarText()

                if(isSearchTextWatcherInitialized())
                    initSearchBar(binding.fragmentImageLibraryAppBarInclude.baseToolbarSearchEditText,
                        frContext.resources.getString(R.string.search_images), searchTextWatcher)

                initSelectionBar(
                        binding.fragmentImageLibrarySelectionBarInclude.layoutSelectionBarRootLayout,
                        binding.fragmentImageLibrarySelectionBarInclude.layoutSelectionBarSelectionCountCb
                ) { _: CompoundButton, b: Boolean ->
                    if(!viewModel.selectionTool.selectionCheckBoxSticky) {
                        viewModel.mainScope.launch {
                            if(b) {
                                viewModel.selectionTool.selectAll(mutableListOf<String>().apply {
                                    if(binding.fragmentImageLibraryList.adapter != null) {
                                        (binding.fragmentImageLibraryList.adapter as ImageLibraryAdapter).currentList.forEach {
                                            add(it.data)
                                        }
                                    }
                                }, binding.fragmentImageLibraryList.adapter!!)
                            } else
                                viewModel.selectionTool.unselectAll(binding.fragmentImageLibraryList.adapter!!)

                            viewModel.selectionTool.initSelectionState(
                                    activity,
                                    binding.root,
                                    binding.fragmentImageLibraryList.adapter!!,
                                    binding.fragmentImageLibraryBottomToolbarInclude.layoutBottomToolbarRootLayout,
                                    binding.fragmentImageLibraryAppBarInclude.baseToolbar,
                                    binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                                    binding.fragmentImageLibrarySelectionBarInclude.layoutSelectionBarRootLayout,
                                    binding.fragmentImageLibrarySelectionBarInclude.layoutSelectionBarSelectionCountCb,
                                    viewModel.selectionTool.selectionMode,
                                    viewModel.selectionTool.selectedPaths.size
                            )
                        }
                    }
                }

                initTabsBar(
                        binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout,

                        if (folderItem != null)
                            binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarFoldersTitle
                        else
                            binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarLibraryTitle,

                        if(folderItem != null)
                            binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarFoldersTitleIndicator
                        else
                            binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarLibraryTitleIndicator,

                        binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarLibraryTitleContainer,
                        binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarLibraryTitle,
                        frContext.resources.getString(R.string.title_gallery),
                        if(folderItem != null) {
                            {
                                activity.supportFragmentManager.popBackStack(FragmentWrapper.NAME_IMAGE_FOLDERS_FRAGMENT, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                            }
                        } else {
                            {

                            }
                        },
                        binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarFoldersTitleContainer,
                        binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarFoldersTitle,
                        if(folderItem != null) {
                            {
                                activity.onBackPressed()
                            }
                        } else {
                            {
                                activity.supportFragmentManager.beginTransaction().replace(activity.binding.activityHomeRootDrawerLayout.id, ImageFoldersFragment()).addToBackStack(FragmentWrapper.NAME_IMAGE_FOLDERS_FRAGMENT).commit()
                            }
                        }
                )

                initToolBar(
                        frContext,
                        binding.fragmentImageLibraryBottomToolbarInclude.layoutBottomToolbarList,
                        mutableListOf<ToolbarItem>().apply {
                            add(
                                    ToolbarItem(
                                        frContext.resources.getString(R.string.toolbar_info),
                                        R.drawable.ic_baseline_info_24,
                                        infoAction
                                    )
                            )
                            add(
                                    ToolbarItem(
                                        frContext.resources.getString(R.string.toolbar_share),
                                        R.drawable.ic_baseline_share_24,
                                        shareAction
                                    )
                            )
                            add(
                                    ToolbarItem(
                                        frContext.resources.getString(R.string.toolbar_move),
                                        R.drawable.ic_baseline_arrow_forward_24,
                                        moveAction
                                    )
                            )
                            add(
                                    ToolbarItem(
                                        frContext.resources.getString(R.string.toolbar_copy),
                                        R.drawable.ic_baseline_file_copy_24,
                                        copyAction
                                    )
                            )
                            add(
                                    ToolbarItem(
                                        frContext.resources.getString(R.string.toolbar_rename),
                                        R.drawable.ic_baseline_edit_24,
                                        renameAction
                                    )
                            )
                            add(
                                    ToolbarItem(
                                        frContext.resources.getString(R.string.toolbar_delete),
                                        R.drawable.ic_baseline_delete_forever_24,
                                        deleteAction
                                    )
                            )
                        },
                        activity.layoutInflater
                )

                initBars(viewModel.selectionTool.selectionMode, binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout, binding.fragmentImageLibraryBottomToolbarInclude.layoutBottomToolbarRootLayout)
                
                restoreLastDialog()

                updateListState()

            }
        }
    }

    override fun restoreLastDialog() {
        if(viewModel.shownDialogs.containsKey(DeleteTool.dialogId)) {
            DeleteTool.dialogShown = false
            deleteAction.invoke()
        }
    }

    override fun onResume() {
        super.onResume()

        PermissionWrapper.handleUserReturnFromAppSettingsForStorageGroup(activity) {
            launchCore()
        }
    }

    private fun updateListState() {
        if(isViewModelInitialized()) {
            CoroutineWrapper.globalIOScope.launch {
                viewModel.shouldScrollToTop = viewModel.currentSortOrder != PreferencesWrapper.getString(frContext, SortTool.KEY_SP_IMAGE_LIBRARY_SORT_ORDER, SortTool.SORT_ORDER_DATE_RECENT)

                viewModel.currentSortOrder = PreferencesWrapper.getString(frContext, SortTool.KEY_SP_IMAGE_LIBRARY_SORT_ORDER, SortTool.SORT_ORDER_DATE_RECENT)

                viewModel.assignItemsLive(frContext, false)
            }
        }
    }

    override fun fetchOldSearchBarText() {
        if(isViewModelInitialized()) {
            if(folderItem == null) {
                viewModel.currentSearchQuery.run {
                    if(viewModel.currentSearchQuery.isBlank()) viewModel.setSearchQuery(null)

                    if(viewModel.currentSearchQuery.isNotEmpty())
                        CoroutineWrapper.globalIOScope.launch {
                            viewModel.assignItemsLive(frContext, false)
                        }

                    if(viewModel.currentSearchQuery.isEmpty()) {
                        binding.fragmentImageLibraryAppBarInclude.baseToolbarSearchEditText.clearFocus()
                    } else if(viewModel.currentSearchQuery.isBlank()) {
                        binding.fragmentImageLibraryAppBarInclude.baseToolbarSearchEditText.clearFocus()
                    } else {
                        binding.fragmentImageLibraryAppBarInclude.baseToolbarSearchEditText.requestFocus()
                    }
                }
            }

            binding.fragmentImageLibraryAppBarInclude.baseToolbarSearchEditText.run {
                text = SpannableStringBuilder(viewModel.currentSearchQuery)
                setSelection(text.length)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.section_app_bar_menu, menu)

        searchBackCallback = initOnBackCallback(activity, object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if(binding.fragmentImageLibraryAppBarInclude.baseToolbarSearchEditText.text.isNotEmpty() && menu.findItem(R.id.sectionAppBarMenuItemClearSearch).isVisible)
                    menu.performIdentifierAction(R.id.sectionAppBarMenuItemClearSearch, 0)
            }
        })

        initSearchClearBtn(
                menu.findItem(R.id.sectionAppBarMenuItemClearSearch),
                if(isViewModelInitialized()) viewModel.currentSearchQuery else ""
        ) {
            if(isViewModelInitialized()) {
                viewModel.iOScope.launch {
                    viewModel.setSearchQuery("")
                    viewModel.assignItemsLive(frContext, false)
                }

                binding.fragmentImageLibraryAppBarInclude.baseToolbarSearchEditText.text = SpannableStringBuilder("")
                if(isSearchBackCallbackInitialized()) searchBackCallback.isEnabled = false

                viewModel.selectionTool.initSelectionState(
                        activity,
                        binding.root,
                        binding.fragmentImageLibraryList.adapter!!,
                        binding.fragmentImageLibraryBottomToolbarInclude.layoutBottomToolbarRootLayout,
                        binding.fragmentImageLibraryAppBarInclude.baseToolbar,
                        binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                        binding.fragmentImageLibrarySelectionBarInclude.layoutSelectionBarRootLayout,
                        binding.fragmentImageLibrarySelectionBarInclude.layoutSelectionBarSelectionCountCb,
                        viewModel.selectionTool.selectionMode,
                        viewModel.selectionTool.selectedPaths.size
                )
            }

            false
        }

        binding.fragmentImageLibraryAppBarInclude.baseToolbarSearchEditText.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI

        searchTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if(isViewModelInitialized()) {
                    CoroutineWrapper.globalIOScope.launch {
                        viewModel.shouldScrollToTop = true
                        viewModel.setSearchQuery(s.toString())
                        viewModel.assignItemsLive(frContext, false)
                    }

                    initSearchClearBtn(menu.findItem(R.id.sectionAppBarMenuItemClearSearch), s.toString())
                    searchBackCallback.isEnabled = s.toString().isNotEmpty()
                }
            }

        }

        menu.findItem(R.id.sectionAppBarMenuItemSort).setOnMenuItemClickListener {
            if(isViewModelInitialized()) {
                SortTool.showSortBottomModalSheetFragment(activity.supportFragmentManager, viewModel)
            }

            true
        }

        menu.findItem(R.id.sectionAppBarMenuItemSettings).setOnMenuItemClickListener {
            hideKeyboard(frContext, binding.root, 0)
            activity.binding.activityHomeRootDrawerLayout.openDrawer(activity.binding.activityHomeSettingsNavView, true)

            true
        }

    }

    override fun onStop() {
        super.onStop()

        if(isViewModelInitialized()) {
            binding.fragmentImageLibraryList.layoutManager.let {
                if(it != null) viewModel.mainListRvState = it.onSaveInstanceState()
            }
        }

        viewModel.shownDialogs.forEach {
            it.value.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if(isViewModelInitialized() && viewModel.contentObserver != null) viewModel.releaseContentObserver(frContext)
    }
}