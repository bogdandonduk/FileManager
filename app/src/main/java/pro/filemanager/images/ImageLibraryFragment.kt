package pro.filemanager.images

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.recyclerview.widget.*
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.layout_bottom_tabs_bar.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import pro.filemanager.ApplicationLoader
import pro.filemanager.R
import pro.filemanager.core.wrappers.PreferencesWrapper
import pro.filemanager.core.SimpleInjector
import pro.filemanager.core.generics.BaseContentObserver
import pro.filemanager.core.ui.UIManager
import pro.filemanager.core.generics.BaseItem
import pro.filemanager.core.generics.BaseSectionFragment
import pro.filemanager.core.tools.DeleteTool
import pro.filemanager.core.tools.ShareTool
import pro.filemanager.core.tools.info.InfoTool
import pro.filemanager.core.tools.rename.RenameTool
import pro.filemanager.core.tools.sort.SortTool
import pro.filemanager.core.tools.toolbar_options.ToolBarOptionItem
import pro.filemanager.core.tools.toolbar_options.ToolbarOptionsTool
import pro.filemanager.databinding.FragmentImageLibraryBinding
import pro.filemanager.files.FileCore
import pro.filemanager.images.folders.ImageFolderItem
import kotlin.math.abs

class ImageLibraryFragment : BaseSectionFragment(), Observer<MutableList<ImageItem>> {

    lateinit var binding: FragmentImageLibraryBinding
    lateinit var viewModel: ImageLibraryViewModel

    var folderItem: ImageFolderItem? = null

    lateinit var onBackCallback: OnBackPressedCallback
    lateinit var searchBackCallback: OnBackPressedCallback

    lateinit var searchView: SearchView

    override fun onChanged(newItems: MutableList<ImageItem>?) {
        if(binding.fragmentImageLibraryList.adapter != null) {
            try {
                viewModel.MainScope?.cancel()
                viewModel.MainScope = CoroutineScope(Main)
            } catch(thr: Throwable) {

            }

            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            if(viewModel.shouldScrollToTop) (binding.fragmentImageLibraryList.adapter as ImageLibraryAdapter).submitList(null)
            (binding.fragmentImageLibraryList.adapter as ImageLibraryAdapter).submitList(newItems)

            viewModel.shouldScrollToTop = false
            viewModel.searchInProgress = false

            notifyListEmpty(newItems!!.size, binding.fragmentImageLibraryNoImagesTitle, binding.fragmentImageLibraryScrollBtn)

            initSelectionState(
                    viewModel,
                    activity,
                    binding.fragmentImageLibraryList.adapter!!,
                    binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarRootLayout,
                    binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                    binding.fragmentImageLibraryAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                    binding.fragmentImageLibraryAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb
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
            navController = Navigation.findNavController(binding.root)

            folderItem = arguments?.getParcelable(FileCore.KEY_ARGUMENT_FOLDER_PARCELABLE)

            setHasOptionsMenu(true)

            activity.setSupportActionBar(binding.fragmentImageLibraryAppBarInclude.layoutBaseToolBarInclude.layoutBaseToolbar)
            activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)

            binding.fragmentImageLibraryAppBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                if(abs(verticalOffset) == appBarLayout.height && !translucentStatusBar) {
                    activity.window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                } else if(verticalOffset == 0 && !translucentStatusBar){
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                }
            })

            activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(folderItem == null) {
                override fun handleOnBackPressed() {
                    navController.popBackStack(R.id.homeFragment, false)
                }
            }.apply {
                onBackCallback = this
            })

            launchCore()

            pinchZoomGestureDetector = ScaleGestureDetector(frContext, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector?): Boolean {
                    if (detector != null) {
                        if (detector.scaleFactor > 1) {
                            if (activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                if((binding.fragmentImageLibraryList.layoutManager as GridLayoutManager).spanCount != 6) {
                                    UIManager.setImageLibraryGridSpanNumberLandscape(activity, 6)
                                    viewModel.MainScope?.launch {
                                        (binding.fragmentImageLibraryList.layoutManager as GridLayoutManager).spanCount = 6
                                        (binding.fragmentImageLibraryList.adapter as ImageLibraryAdapter).run {
                                            submitList(currentList)
                                        }
                                    }
                                }
                            } else {
                                if((binding.fragmentImageLibraryList.layoutManager as GridLayoutManager).spanCount != 4) {
                                    UIManager.setImageLibraryGridSpanNumberPortrait(activity, 4)
                                    viewModel.MainScope?.launch {
                                        (binding.fragmentImageLibraryList.layoutManager as GridLayoutManager).spanCount = 4
                                        (binding.fragmentImageLibraryList.adapter as ImageLibraryAdapter).run {
                                            submitList(currentList)
                                        }
                                    }
                                }
                            }
                        } else {
                            if (activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                if((binding.fragmentImageLibraryList.layoutManager as GridLayoutManager).spanCount != 10) {
                                    UIManager.setImageLibraryGridSpanNumberLandscape(activity, 10)
                                    viewModel.MainScope?.launch {
                                        (binding.fragmentImageLibraryList.layoutManager as GridLayoutManager).spanCount = 10
                                        (binding.fragmentImageLibraryList.adapter as ImageLibraryAdapter).run {
                                            submitList(currentList)
                                        }
                                    }
                                }
                            } else {
                                if((binding.fragmentImageLibraryList.layoutManager as GridLayoutManager).spanCount != 6) {
                                    UIManager.setImageLibraryGridSpanNumberPortrait(activity, 6)
                                    viewModel.MainScope?.launch {
                                        (binding.fragmentImageLibraryList.layoutManager as GridLayoutManager).spanCount = 6
                                        (binding.fragmentImageLibraryList.adapter as ImageLibraryAdapter).run {
                                            submitList(currentList)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return true
                }

                override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
                    return true
                }

                override fun onScaleEnd(detector: ScaleGestureDetector?) {

                }
            })
        } catch(thr: Throwable) {

        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initList(imageItems: MutableList<ImageItem>) {
        binding.fragmentImageLibraryList.layoutManager = GridLayoutManager(context, UIManager.getImageLibraryGridSpanNumber(activity))

        binding.fragmentImageLibraryList.layoutManager!!.onRestoreInstanceState(viewModel.mainListRvState)
        viewModel.mainListRvState = null

        binding.fragmentImageLibraryList.adapter = ImageLibraryAdapter(requireActivity(), imageItems, layoutInflater, this@ImageLibraryFragment).apply {
            setHasStableIds(true)
        }

        binding.fragmentImageLibraryList.itemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean = true
        }

        (binding.fragmentImageLibraryList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        binding.fragmentImageLibraryList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dx > 0 || dy > 0) {
                    if(this@ImageLibraryFragment::viewModel.isInitialized) {
                        if(tabsBarVisible) {
                            if(!viewModel.selectionTool.selectionMode) {
                                binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout.animate().alpha(0f).setDuration(300).start()
                                activity.handler.postDelayed({
                                    binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.GONE
                                }, 300)
                            }
                            tabsBarVisible = false
                        }

                        if(toolBarVisible) {
                            binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarRootLayout.animate().alpha(0f).setDuration(300).start()
                            activity.handler.postDelayed({
                                binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarRootLayout.visibility = View.GONE
                            }, 300)
                            toolBarVisible = false
                        }

                        if(scrollBtnVisible) {
                            binding.fragmentImageLibraryScrollBtnIcon.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)

                            binding.fragmentImageLibraryScrollBtn.setOnClickListener {
                                binding.fragmentImageLibraryList.scrollToPosition(if(binding.fragmentImageLibraryList.adapter!!.itemCount > 0) binding.fragmentImageLibraryList.adapter!!.itemCount - 1 else 0)
                            }
                        }
                    }
                } else {
                    if(this@ImageLibraryFragment::viewModel.isInitialized) {
                        if(!viewModel.selectionTool.selectionMode) binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.VISIBLE
                        if(viewModel.selectionTool.selectionMode) binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarRootLayout.visibility = View.VISIBLE

                        if(!tabsBarVisible) {
                            if (!viewModel.selectionTool.selectionMode) binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout.animate().alpha(1f).setDuration(300).start()
                            tabsBarVisible = true
                        }

                        if(!toolBarVisible) {
                            binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarRootLayout.animate().alpha(1f).setDuration(300).start()
                            toolBarVisible = true
                        }

                        if(scrollBtnVisible) {
                            binding.fragmentImageLibraryScrollBtnIcon.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)

                            binding.fragmentImageLibraryScrollBtn.setOnClickListener {
                                binding.fragmentImageLibraryList.scrollToPosition(0)
                            }
                        }
                    }
                }
            }
        })

        binding.fragmentImageLibraryList.setOnTouchListener { _, event ->
            pinchZoomGestureDetector.onTouchEvent(event)
            false
        }

        notifyListEmpty(imageItems.size, binding.fragmentImageLibraryNoImagesTitle, binding.fragmentImageLibraryScrollBtn)
    }

    override fun launchCore() {
        activity.requestExternalStoragePermission {
            ApplicationLoader.ApplicationIOScope.launch {
                viewModel = ViewModelProvider(this@ImageLibraryFragment, SimpleInjector.provideImageLibraryViewModelFactory(frContext, folderItem)).get(ImageLibraryViewModel::class.java)

                withContext(Main) {
                    viewModel.getItemsLive(frContext).observe(viewLifecycleOwner, this@ImageLibraryFragment)

                    initList(viewModel.getItemsLive(frContext).value!!)

                    binding.fragmentImageLibraryListProgressBar.visibility = View.GONE

                    if(viewModel.contentObserver == null) viewModel.contentObserver = BaseContentObserver(frContext, viewModel, activity.handler)
                    frContext.contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, viewModel.contentObserver!!)

                    initSelectionState(
                            viewModel,
                            activity,
                            binding.fragmentImageLibraryList.adapter!!,
                            binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarRootLayout,
                            binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                            binding.fragmentImageLibraryAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                            binding.fragmentImageLibraryAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb
                    )

                    initSelectionBar(
                            binding.fragmentImageLibraryAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb
                    ) { _: CompoundButton, b: Boolean ->
                        if(!viewModel.selectionTool.selectionCheckBoxSticky) {
                            viewModel.MainScope?.launch {
                                if(b) {
                                    viewModel.selectionTool.selectAll(mutableListOf<String>().apply {
                                        if (binding.fragmentImageLibraryList.adapter != null) {
                                            (binding.fragmentImageLibraryList.adapter as ImageLibraryAdapter).currentList.forEach {
                                                add(it.data)
                                            }
                                        }
                                    }, binding.fragmentImageLibraryList.adapter!!)
                                } else {
                                    viewModel.selectionTool.unselectAll(binding.fragmentImageLibraryList.adapter!!)
                                }
                            }
                        }
                    }

                    initTabsBar(
                            binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout,

                            if(folderItem != null)
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
                                    ApplicationLoader.folderFragmentImmediateAction = Runnable {
                                        activity.onBackPressed()

                                        ApplicationLoader.folderFragmentImmediateAction = null
                                    }

                                    activity.onBackPressed()
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
                                    try {
                                        navController.navigate(R.id.action_imageLibraryFragment_to_imageFoldersFragment,
                                                null,
                                                if(this@ImageLibraryFragment.folderItem != null)
                                                    NavOptions.Builder()
                                                            .setEnterAnim(R.anim.fragment_close_enter)
                                                            .build()
                                                else null
                                        )
                                    } catch(thr: Throwable) {

                                    }
                                }
                            }
                    )

                    initToolBar(
                            binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarRootLayout,
                            mutableListOf<ViewGroup>().apply {
                                add(binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarOptionsContainer)
                                add(binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarInfoContainer)
                                add(binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarDeleteContainer)
                            },
                            mutableListOf<View.OnClickListener>().apply {
                                add {
                                    ToolbarOptionsTool.setToolBarOptions(viewModel, mutableListOf<ToolBarOptionItem>().apply {
                                        add(
                                                ToolBarOptionItem(
                                                        frContext.resources.getString(R.string.toolbar_share),
                                                        R.drawable.ic_baseline_share_24
                                                ) {
                                                    if(ToolbarOptionsTool.currentViewModel!!.selectionTool.selectedPaths.isNotEmpty()) {
                                                        ApplicationLoader.ApplicationIOScope.launch {
                                                            withContext(Main) {
                                                                if(ToolbarOptionsTool.currentBottomModalSheetFragment != null && ToolbarOptionsTool.currentBottomModalSheetFragment!!.showsDialog)
                                                                    ToolbarOptionsTool.currentBottomModalSheetFragment!!.dismiss()
                                                            }

                                                            ShareTool.shareImages(frContext, ToolbarOptionsTool.currentViewModel!!.selectionTool.selectedPaths)
                                                        }
                                                    }
                                                }
                                        )
                                        add(
                                                ToolBarOptionItem(
                                                        frContext.resources.getString(R.string.toolbar_move),
                                                        R.drawable.ic_baseline_arrow_forward_24
                                                ) {
                                                    ApplicationLoader.ApplicationIOScope.launch {

                                                        withContext(Main) {
                                                            if(ToolbarOptionsTool.currentBottomModalSheetFragment != null && ToolbarOptionsTool.currentBottomModalSheetFragment!!.showsDialog)
                                                                ToolbarOptionsTool.currentBottomModalSheetFragment!!.dismiss()
                                                        }
                                                    }
                                                }
                                        )
                                        add(
                                                ToolBarOptionItem(
                                                        frContext.resources.getString(R.string.toolbar_copy),
                                                        R.drawable.ic_baseline_file_copy_24
                                                ) {
                                                    ApplicationLoader.ApplicationIOScope.launch {

                                                        withContext(Main) {
                                                            if(ToolbarOptionsTool.currentBottomModalSheetFragment != null && ToolbarOptionsTool.currentBottomModalSheetFragment!!.showsDialog)
                                                                ToolbarOptionsTool.currentBottomModalSheetFragment!!.dismiss()
                                                        }
                                                    }
                                                }
                                        )
                                        add(
                                                ToolBarOptionItem(
                                                        frContext.resources.getString(R.string.toolbar_rename),
                                                        R.drawable.ic_baseline_edit_24
                                                ) {
                                                    if(viewModel.selectionTool.selectedPaths.size == 1) {
                                                        ApplicationLoader.ApplicationIOScope.launch {
                                                            withContext(Main) {
                                                                if(ToolbarOptionsTool.currentBottomModalSheetFragment != null && ToolbarOptionsTool.currentBottomModalSheetFragment!!.showsDialog)
                                                                    ToolbarOptionsTool.currentBottomModalSheetFragment!!.dismiss()
                                                            }

                                                            try {
                                                                RenameTool.showRenameBottomModalSheetFragment(activity.supportFragmentManager, viewModel.selectionTool.selectedPaths[0]) {
                                                                    try {
                                                                        ApplicationLoader.ApplicationIOScope.launch {
                                                                            viewModel.assignItemsLive(frContext, true)
                                                                            ImageRepo.getSingleton().loadAll(frContext, true)

                                                                            viewModel.selectionTool.selectionMode = false

                                                                            withContext(Main) {
                                                                                initSelectionState(
                                                                                    viewModel,
                                                                                    activity,
                                                                                    binding.fragmentImageLibraryList.adapter!!,
                                                                                    binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarRootLayout,
                                                                                    binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                                                                                    binding.fragmentImageLibraryAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                                                                                    binding.fragmentImageLibraryAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb
                                                                                )
                                                                            }
                                                                        }
                                                                    } catch (thr: Throwable) {

                                                                    }
                                                                }
                                                            } catch(thr: Throwable) {

                                                            }

                                                        }
                                                    }
                                                }
                                        )
                                    })

                                    ToolbarOptionsTool.showToolBarBottomModalSheetFragment(activity.supportFragmentManager)
                                }
                                add {
                                    if(viewModel.selectionTool.selectedPaths.isNotEmpty()) {
                                        InfoTool.showInfoItemBottomModalSheetFragment(
                                                activity.supportFragmentManager,
                                                mutableListOf<BaseItem>().apply {
                                                    (binding.fragmentImageLibraryList.adapter as ImageLibraryAdapter).currentList.forEach {
                                                        if (viewModel.selectionTool.selectedPaths.contains(it.data)) {
                                                            add(it)
                                                        }
                                                    }
                                                }
                                        )
                                    }
                                }
                                add {
                                    if(viewModel.selectionTool.selectedPaths.isNotEmpty()) {
                                        try {
                                            DeleteTool.deleteItemsAndRefreshMediaStore(activity, viewModel.selectionTool.selectedPaths) {
                                                ApplicationLoader.ApplicationIOScope.launch {
                                                    try {
                                                        viewModel.assignItemsLive(frContext, true)
                                                        ImageRepo.getSingleton().loadAll(frContext, true)

                                                        viewModel.selectionTool.selectionMode = false

                                                        withContext(Main) {
                                                            initSelectionState(
                                                                viewModel,
                                                                activity,
                                                                binding.fragmentImageLibraryList.adapter!!,
                                                                binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarRootLayout,
                                                                binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                                                                binding.fragmentImageLibraryAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                                                                binding.fragmentImageLibraryAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb
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
                            },
                            mutableListOf<TextView>().apply {
                                add(binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarOptionsTitle)
                                add(binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarInfoTitle)
                                add(binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarDeleteTitle)
                            }
                    )

                    restoreLastDialog()
                }

                updateListState()
            }
        }
    }

    override fun restoreLastDialog() {
        if(DeleteTool.lastDialogShown) {
            binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarDeleteContainer.callOnClick()
        }
    }

    override fun onResume() {
        super.onResume()

        activity.supportActionBar?.title = folderItem?.displayName ?: frContext.resources.getString(R.string.title_images)

        handleUserReturnFromAppSettings(activity)
    }

    override fun updateListState() {
        if(this::viewModel.isInitialized) {
            viewModel.IOScope.launch {
                viewModel.shouldScrollToTop = viewModel.currentSortOrder != PreferencesWrapper.getString(frContext, SortTool.KEY_SP_IMAGE_LIBRARY_SORT_ORDER, SortTool.SORT_ORDER_DATE_RECENT)

                viewModel.currentSortOrder = PreferencesWrapper.getString(frContext, SortTool.KEY_SP_IMAGE_LIBRARY_SORT_ORDER, SortTool.SORT_ORDER_DATE_RECENT)

                viewModel.assignItemsLive(frContext, false)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_toolbar_menu, menu)

        searchView = menu.findItem(R.id.mainToolbarMenuItemSearch).actionView as SearchView

        activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                searchView.isIconified = true
                searchView.clearFocus()
            }
        }.apply {
            searchBackCallback = this
        })

        searchView.apply {
            searchView.queryHint = frContext.resources.getString(R.string.title_search_hint)

            imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI

            setOnSearchClickListener {
                if(this@ImageLibraryFragment::viewModel.isInitialized)
                    viewModel.isSearchViewEnabled = true
            }

            setOnCloseListener {
                if(this@ImageLibraryFragment::viewModel.isInitialized)
                    viewModel.isSearchViewEnabled = false

                false
            }

            if(this@ImageLibraryFragment::viewModel.isInitialized && folderItem == null) {
                ApplicationLoader.transientStrings[UIManager.KEY_TRANSIENT_STRINGS_IMAGE_LIBRARY_SEARCH_TEXT].let {
                    if(it != null) {
                        viewModel.isSearchViewEnabled = true
                        viewModel.setSearchText(it)

                        if(viewModel.currentSearchText.isBlank()) viewModel.setSearchText(null)

                        if(viewModel.currentSearchText.isNotEmpty())
                            viewModel.IOScope.launch {
                                viewModel.assignItemsLive(frContext, false)
                            }

                        setQuery(viewModel.currentSearchText, false)
                        isIconified = false

                        if(viewModel.currentSearchText.isEmpty()) {
                            clearFocus()
                        } else if(viewModel.currentSearchText.isBlank()) {
                            clearFocus()
                        } else {
                            requestFocus()
                        }

                    }

                    ApplicationLoader.transientStrings.remove(UIManager.KEY_TRANSIENT_STRINGS_IMAGE_LIBRARY_SEARCH_TEXT)
                }
            }

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if(this@ImageLibraryFragment::viewModel.isInitialized) {
                        viewModel.IOScope.launch {
                            viewModel.shouldScrollToTop = true
                            viewModel.setSearchText(newText)
                            viewModel.assignItemsLive(frContext, false)
                        }

                        searchBackCallback.isEnabled = !newText.isNullOrEmpty()
                    }

                    return false
                }
            })
        }

        menu.findItem(R.id.mainToolbarMenuItemSort).setOnMenuItemClickListener {
            if(this@ImageLibraryFragment::viewModel.isInitialized) {
                SortTool.showSortBottomModalSheetFragment(activity.supportFragmentManager, viewModel)
            }

            true
        }
    }

    override fun onStop() {
        super.onStop()

        if(this::viewModel.isInitialized) {
            if(this::searchView.isInitialized && folderItem == null)
                if(viewModel.isSearchViewEnabled) ApplicationLoader.transientStrings[UIManager.KEY_TRANSIENT_STRINGS_IMAGE_LIBRARY_SEARCH_TEXT] = searchView.query.toString()
            viewModel.mainListRvState = binding.fragmentImageLibraryList.layoutManager?.onSaveInstanceState()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if(viewModel.contentObserver != null) frContext.contentResolver.unregisterContentObserver(viewModel.contentObserver!!)
    }
}