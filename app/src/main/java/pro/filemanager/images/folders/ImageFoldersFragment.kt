package pro.filemanager.images.folders

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
import kotlinx.android.synthetic.main.layout_bottom_toolbar.view.*
import kotlinx.coroutines.*
import pro.filemanager.R
import pro.filemanager.core.*
import pro.filemanager.core.base.BaseFolderItem
import pro.filemanager.core.base.BaseFragment
import pro.filemanager.core.tools.info.InfoTool
import pro.filemanager.core.tools.sort.SortTool
import pro.filemanager.core.tools.toolbar.ToolbarItem
import pro.filemanager.core.ui.FragmentWrapper
import pro.filemanager.core.ui.UIManager
import pro.filemanager.core.wrappers.CoroutineWrapper
import pro.filemanager.core.wrappers.PreferencesWrapper
import pro.filemanager.databinding.FragmentImageFoldersBinding
import pro.filemanager.home.HomeActivity
import pro.filemanager.images.ImageRepo

@Suppress("UNCHECKED_CAST")
class ImageFoldersFragment : BaseFragment(), Observer<MutableList<ImageFolderItem>> {
    lateinit var viewModel: ImageFoldersViewModel
    lateinit var binding: FragmentImageFoldersBinding

    fun isViewModelInitialized() : Boolean = this::viewModel.isInitialized

    fun isBindingInitialized() : Boolean = this::binding.isInitialized

    private val mainListScrollDownAction = {
        if(isViewModelInitialized()) {
            if(tabsBarVisible) {
                if(!viewModel.selectionTool.selectionMode) {
                    binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout.animate().alpha(0f).setDuration(200).start()
                    activity.handler.postDelayed({
                        binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.GONE
                    }, 200)
                }
                tabsBarVisible = false
            }

            if(toolBarVisible) {
                binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolbarRootLayout.animate().alpha(0f).setDuration(200).start()
                activity.handler.postDelayed({
                    binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolbarRootLayout.visibility = View.GONE
                }, 200)
                toolBarVisible = false
            }

            scrollDownBtnInitializer.invoke()
        }
    }

    val scrollDownBtnInitializer = {
        binding.fragmentImageFoldersScrollBtnInclude.layoutScrollBtnIcon.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)

        binding.fragmentImageFoldersScrollBtnInclude.layoutScrollBtnIcon.setOnClickListener {
            binding.fragmentImageFoldersList.scrollToPosition(if(binding.fragmentImageFoldersList.adapter!!.itemCount > 0) binding.fragmentImageFoldersList.adapter!!.itemCount - 1 else 0)
        }
    }

    private val mainListScrollUpAction = {
        if(isViewModelInitialized()) {
            if(!viewModel.selectionTool.selectionMode) {
                binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.VISIBLE
                binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolbarRootLayout.visibility = View.GONE
            } else {
                binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolbarRootLayout.visibility = View.VISIBLE
                binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.GONE
            }

            if(!tabsBarVisible) {
                if(!viewModel.selectionTool.selectionMode) binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout.animate().alpha(1f).setDuration(200).start()
                tabsBarVisible = true
            }

            if(!toolBarVisible) {
                binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolbarRootLayout.animate().alpha(1f).setDuration(200).start()
                toolBarVisible = true
            }

            scrollUpBtnInitializer.invoke()
        }
    }

    val scrollUpBtnInitializer = {
        binding.fragmentImageFoldersScrollBtnInclude.layoutScrollBtnIcon.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)

        binding.fragmentImageFoldersScrollBtnInclude.layoutScrollBtnIcon.setOnClickListener {
            binding.fragmentImageFoldersList.scrollToPosition(0)
        }
    }

    override fun onChanged(newItems: MutableList<ImageFolderItem>?) {
        if(isViewModelInitialized() && isBindingInitialized() && binding.fragmentImageFoldersList.adapter != null) {
            try {
                viewModel.resetCoroutineScopeMain()
            } catch(thr: Throwable) {

            }

            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            refreshListAdapter(binding.fragmentImageFoldersList.adapter as ListAdapter<ImageFolderItem, RecyclerView.ViewHolder>, newItems!!, viewModel.shouldScrollToTop)

            viewModel.resetFlags()

            notifyListEmpty(
                    newItems.size,
                    binding.fragmentImageFoldersNoFoldersTitle,
                    binding.fragmentImageFoldersGridResizeBtnInclude.layoutGridResizeBtnRootLayout,
                    binding.fragmentImageFoldersScrollBtnInclude.layoutScrollBtnRootLayout,
                    binding.fragmentImageFoldersList
            )

            viewModel.selectionTool.initSelectionState(
                    activity,
                    binding.root,
                    binding.fragmentImageFoldersList.adapter!!,
                    binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolbarRootLayout,
                    binding.fragmentImageFoldersAppBarInclude.baseToolbar,
                    binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                    binding.fragmentImageFoldersSelectionBarInclude.layoutSelectionBarRootLayout,
                    binding.fragmentImageFoldersSelectionBarInclude.layoutSelectionBarSelectionCountCb,
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
        binding = FragmentImageFoldersBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            initAppBar(frContext, binding.fragmentImageFoldersAppBarInclude.baseToolbar, null, R.drawable.ic_baseline_arrow_back_24, frContext.resources.getString(R.string.go_back)) {
                hideKeyboard(frContext, view, 0)
                activity.onBackPressed()
            }

            launchCore()
        } catch(thr: Throwable) {

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    suspend fun initList(imageFolderItems: MutableList<ImageFolderItem>) {
        initListGridLayoutManager(
                binding.fragmentImageFoldersList,
                UIManager.getImageFoldersGridSpanNumber(activity),
                VolatileValueHolder.parcelables[UIManager.KEY_TRANSIENT_PARCELABLE_IMAGE_FOLDERS_MAIN_LIST_RV_STATE],
                null
        ) {
            VolatileValueHolder.parcelables.remove(UIManager.KEY_TRANSIENT_PARCELABLE_IMAGE_FOLDERS_MAIN_LIST_RV_STATE)
        }

        binding.fragmentImageFoldersList.adapter = ImageFoldersAdapter(requireActivity(), imageFolderItems, layoutInflater, this).apply {
            setHasStableIds(true)
        }

        initListAnimator(binding.fragmentImageFoldersList)

        initListGridResizeBtn(
                activity,
                binding.fragmentImageFoldersGridResizeBtnInclude.layoutGridResizeBtnRootLayout,
                binding.fragmentImageFoldersGridResizeBtnInclude.layoutGridResizeBtnTitle,
                binding.fragmentImageFoldersList,
                binding.fragmentImageFoldersList.adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>,
                viewModel
        ) {
            UIManager.resizeImageFoldersListGrid(activity)
        }

        setGridResizeBtnText(
                activity,
                binding.fragmentImageFoldersGridResizeBtnInclude.layoutGridResizeBtnTitle,
                (binding.fragmentImageFoldersList.layoutManager as GridLayoutManager).spanCount
        )

        initListScrolling(binding.fragmentImageFoldersList, mainListScrollDownAction, mainListScrollUpAction)

        notifyListEmpty(
                imageFolderItems.size,
                binding.fragmentImageFoldersNoFoldersTitle,
                binding.fragmentImageFoldersGridResizeBtnInclude.layoutGridResizeBtnRootLayout,
                binding.fragmentImageFoldersScrollBtnInclude.layoutScrollBtnRootLayout,
                binding.fragmentImageFoldersList
        )
    }

    private fun quitToGallery() {
        onBackCallback.isEnabled = false
        if(isSearchBackCallbackInitialized()) searchBackCallback.isEnabled = false
        activity.onBackPressed()
    }

    override fun setGridResizeBtnText(activity: HomeActivity, resizeBtnText: TextView, currentSpanCount: Int) {
        if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
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
                    resizeBtnText.text = (3).toString()
                }
            }
        } else {
            when(currentSpanCount) {
                2 -> {
                    resizeBtnText.text = (3).toString()
                }
                3 -> {
                    resizeBtnText.text = (4).toString()
                }
                4 -> {
                    resizeBtnText.text = (2).toString()
                }
                else -> {
                    resizeBtnText.text = (2).toString()
                }
            }
        }
    }

    override fun launchCore() {
        PermissionWrapper.requestStorageGroup(
                activity,
                binding.fragmentImageFoldersRootLayout
        ) {
            CoroutineWrapper.globalMainScope.launch {
                viewModel = ViewModelProvider(this@ImageFoldersFragment, ImageFoldersViewModelFactory(frContext, ImageRepo.getSingleton())).get(ImageFoldersViewModel::class.java)

                viewModel.librarySortOrder = PreferencesWrapper.getString(frContext, SortTool.KEY_SP_IMAGE_LIBRARY_SORT_ORDER, SortTool.SORT_ORDER_DATE_RECENT)
                viewModel.getItemsLive(frContext).observe(viewLifecycleOwner, this@ImageFoldersFragment)

                initList(viewModel.getItemsLive(frContext).value!!)

                binding.fragmentImageFoldersListProgressBar.visibility = View.GONE

                initAppBarLayoutCollapsing(binding.fragmentImageFoldersAppBarLayout, {
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                }) {
                    activity.window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                }

                onBackCallback = initOnBackCallback(activity, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        activity.supportFragmentManager.popBackStack(FragmentWrapper.NAME_IMAGE_LIBRARY_FRAGMENT, FragmentManager.POP_BACK_STACK_INCLUSIVE)
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
                        binding.fragmentImageFoldersList.adapter!!,
                        binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolbarRootLayout,
                        binding.fragmentImageFoldersAppBarInclude.baseToolbar,
                        binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                        binding.fragmentImageFoldersSelectionBarInclude.layoutSelectionBarRootLayout,
                        binding.fragmentImageFoldersSelectionBarInclude.layoutSelectionBarSelectionCountCb,
                        viewModel.selectionTool.selectionMode,
                        viewModel.selectionTool.selectedPaths.size
                )

                fetchOldSearchBarText()

                if(isSearchTextWatcherInitialized())
                    initSearchBar(binding.fragmentImageFoldersAppBarInclude.baseToolbarSearchEditText,
                        frContext.resources.getString(R.string.search_folders), searchTextWatcher)

                initSelectionBar(
                        binding.fragmentImageFoldersSelectionBarInclude.layoutSelectionBarRootLayout,
                        binding.fragmentImageFoldersSelectionBarInclude.layoutSelectionBarSelectionCountCb
                ) { _: CompoundButton, b: Boolean ->
                    if(!viewModel.selectionTool.selectionCheckBoxSticky) {
                        viewModel.mainScope.launch {
                            if(b) {
                                viewModel.selectionTool.selectAll(mutableListOf<String>().apply {
                                    if(binding.fragmentImageFoldersList.adapter != null) {
                                        (binding.fragmentImageFoldersList.adapter as ImageFoldersAdapter).currentList.forEach {
                                            add(it.data)
                                        }
                                    }
                                }, binding.fragmentImageFoldersList.adapter!!)
                            } else
                                viewModel.selectionTool.unselectAll(binding.fragmentImageFoldersList.adapter!!)

                            viewModel.selectionTool.initSelectionState(
                                    activity,
                                    binding.root,
                                    binding.fragmentImageFoldersList.adapter!!,
                                    binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolbarRootLayout,
                                    binding.fragmentImageFoldersAppBarInclude.baseToolbar,
                                    binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                                    binding.fragmentImageFoldersSelectionBarInclude.layoutSelectionBarRootLayout,
                                    binding.fragmentImageFoldersSelectionBarInclude.layoutSelectionBarSelectionCountCb,
                                    viewModel.selectionTool.selectionMode,
                                    viewModel.selectionTool.selectedPaths.size
                            )
                        }
                    }
                }

                initTabsBar(
                        binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                        binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarFoldersTitle,
                        binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarFoldersTitleIndicator,
                        binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarLibraryTitleContainer,
                        binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarLibraryTitle,
                        frContext.resources.getString(R.string.title_gallery),
                        {
                            quitToGallery()
                        },
                        binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarFoldersTitleContainer,
                        binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarFoldersTitle,
                        {

                        },
                )

                initToolBar(
                        frContext,
                        binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolbarList,
                        mutableListOf<ToolbarItem>().apply {
                            add(
                                    ToolbarItem(
                                            frContext.resources.getString(R.string.toolbar_info),
                                            R.drawable.ic_baseline_info_24
                                    ) {
                                        if(viewModel.selectionTool.selectedPaths.isNotEmpty()) {
                                            InfoTool.showInfoForFolderItems(
                                                    activity.supportFragmentManager,
                                                    mutableListOf<BaseFolderItem>().apply {
                                                        (binding.fragmentImageFoldersList.adapter as ImageFoldersAdapter).imageFolderItems.forEach {
                                                            if(viewModel.selectionTool.selectedPaths.contains(it.data)) {
                                                                add(it)
                                                            }
                                                        }
                                                    }
                                            )
                                        }
                                    }
                            )
                        },
                        activity.layoutInflater
                )

                restoreLastDialog()
            }
        }
    }

    override fun restoreLastDialog() {

    }

    override fun fetchOldSearchBarText() {
        if(isViewModelInitialized()) {
            VolatileValueHolder.strings[UIManager.KEY_TRANSIENT_STRINGS_IMAGE_FOLDERS_SEARCH_TEXT].let {
                if(it != null) {
                    viewModel.setSearchQuery(it)

                    if(viewModel.currentSearchQuery.isBlank()) viewModel.setSearchQuery(null)

                    if(viewModel.currentSearchQuery.isNotEmpty())
                        CoroutineWrapper.globalIOScope.launch {
                            viewModel.assignItemsLive(frContext, false)
                        }

                    if(viewModel.currentSearchQuery.isEmpty()) {
                        binding.fragmentImageFoldersAppBarInclude.baseToolbarSearchEditText.clearFocus()
                    } else if(viewModel.currentSearchQuery.isBlank()) {
                        binding.fragmentImageFoldersAppBarInclude.baseToolbarSearchEditText.clearFocus()
                    } else {
                        binding.fragmentImageFoldersAppBarInclude.baseToolbarSearchEditText.requestFocus()
                    }

                    VolatileValueHolder.strings.remove(UIManager.KEY_TRANSIENT_STRINGS_IMAGE_FOLDERS_SEARCH_TEXT)
                }
            }

            binding.fragmentImageFoldersAppBarInclude.baseToolbarSearchEditText.run {
                text = SpannableStringBuilder(viewModel.currentSearchQuery)
                setSelection(text.length)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        PermissionWrapper.handleUserReturnFromAppSettingsForStorageGroup(activity) {
            launchCore()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.section_app_bar_menu, menu)

        searchBackCallback = initOnBackCallback(activity, object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if(binding.fragmentImageFoldersAppBarInclude.baseToolbarSearchEditText.text.isNotEmpty() && menu.findItem(R.id.sectionAppBarMenuItemClearSearch).isVisible)
                    menu.performIdentifierAction(R.id.sectionAppBarMenuItemClearSearch, 0)
            }
        })

        initSearchClearBtn(
                menu.findItem(R.id.sectionAppBarMenuItemClearSearch),
                VolatileValueHolder.strings[UIManager.KEY_TRANSIENT_STRINGS_IMAGE_FOLDERS_SEARCH_TEXT] ?: ""
        ) {
            if(isViewModelInitialized()) {
                viewModel.iOScope.launch {
                    viewModel.setSearchQuery("")
                    viewModel.assignItemsLive(frContext, false)
                }

                binding.fragmentImageFoldersAppBarInclude.baseToolbarSearchEditText.text = SpannableStringBuilder("")
                if(isSearchBackCallbackInitialized()) searchBackCallback.isEnabled = false

                viewModel.selectionTool.initSelectionState(
                        activity,
                        binding.root,
                        binding.fragmentImageFoldersList.adapter!!,
                        binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolbarRootLayout,
                        binding.fragmentImageFoldersAppBarInclude.baseToolbar,
                        binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                        binding.fragmentImageFoldersSelectionBarInclude.layoutSelectionBarRootLayout,
                        binding.fragmentImageFoldersSelectionBarInclude.layoutSelectionBarSelectionCountCb,
                        viewModel.selectionTool.selectionMode,
                        viewModel.selectionTool.selectedPaths.size
                )
            }

            false
        }

        binding.fragmentImageFoldersAppBarInclude.baseToolbarSearchEditText.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI

        searchTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (isViewModelInitialized()) {
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

    override fun onPause() {
        super.onPause()

        if(isViewModelInitialized()) {
            VolatileValueHolder.strings[UIManager.KEY_TRANSIENT_STRINGS_IMAGE_FOLDERS_SEARCH_TEXT] = binding.fragmentImageFoldersAppBarInclude.baseToolbarSearchEditText.text.toString()

            binding.fragmentImageFoldersList.layoutManager.let {
                if(it != null) VolatileValueHolder.parcelables[UIManager.KEY_TRANSIENT_PARCELABLE_IMAGE_FOLDERS_MAIN_LIST_RV_STATE] = binding.fragmentImageFoldersList.layoutManager?.onSaveInstanceState()
            }
        }

        viewModelStore.clear()
    }

    override fun onDestroy() {
        super.onDestroy()

        if(isViewModelInitialized() && viewModel.contentObserver != null) viewModel.releaseContentObserver(frContext)
    }
}