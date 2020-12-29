package pro.filemanager.images.folders

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import pro.filemanager.ApplicationLoader
import pro.filemanager.R
import pro.filemanager.core.*
import pro.filemanager.core.generics.BaseContentObserver
import pro.filemanager.core.generics.BaseFolderItem
import pro.filemanager.core.generics.BaseSectionFragment
import pro.filemanager.core.tools.info.InfoTool
import pro.filemanager.core.tools.sort.SortTool
import pro.filemanager.core.ui.UIManager
import pro.filemanager.core.wrappers.PreferencesWrapper
import pro.filemanager.databinding.FragmentImageFoldersBinding
import pro.filemanager.images.ImageLibraryAdapter
import kotlin.math.abs

class ImageFoldersFragment : BaseSectionFragment(), Observer<MutableList<ImageFolderItem>> {

    lateinit var binding: FragmentImageFoldersBinding
    lateinit var viewModel: ImageFoldersViewModel

    lateinit var onBackCallback: OnBackPressedCallback
    lateinit var searchBackCallback: OnBackPressedCallback

    lateinit var searchView: SearchView

    override fun onChanged(newItems: MutableList<ImageFolderItem>?) {
        if(binding.fragmentImageFoldersList.adapter != null) {
            try {
                viewModel.MainScope?.cancel()
                viewModel.MainScope = CoroutineScope(Main)
            } catch(thr: Throwable) {

            }

            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            if(viewModel.shouldScrollToTop) (binding.fragmentImageFoldersList.adapter as ImageFoldersAdapter).submitList(null)
            (binding.fragmentImageFoldersList.adapter as ImageFoldersAdapter).submitList(newItems)

            viewModel.shouldScrollToTop = false
            viewModel.searchInProgress = false

            notifyListEmpty(newItems!!.size, binding.fragmentImageFoldersNoFoldersTitle, binding.fragmentImageFoldersScrollBtn)
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
            navController = Navigation.findNavController(binding.root)

            setHasOptionsMenu(true)

            activity.setSupportActionBar(binding.fragmentImageFoldersAppBarInclude.layoutBaseToolBarInclude.layoutBaseToolbar)
            activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)

            binding.fragmentImageFoldersAppBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                if(abs(verticalOffset) == appBarLayout.height && !translucentStatusBar) {
                    activity.window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                } else if(verticalOffset == 0 && !translucentStatusBar){
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                }
            })

            activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(ApplicationLoader.folderFragmentImmediateAction == null) {
                override fun handleOnBackPressed() {
                    navController.popBackStack(R.id.homeFragment, false)
                }
            }.apply {
                onBackCallback = this
            })

            ApplicationLoader.folderFragmentImmediateAction?.run() ?: launchCore()
            onBackCallback.isEnabled = true

            pinchZoomGestureDetector = ScaleGestureDetector(frContext, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector?): Boolean {
                    if (detector != null) {
                        if (detector.scaleFactor > 1) {
                            if (activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                if((binding.fragmentImageFoldersList.layoutManager as GridLayoutManager).spanCount != 4) {
                                    UIManager.setImageFoldersGridSpanNumberLandscape(activity, 4)
                                    viewModel.MainScope?.launch {
                                        (binding.fragmentImageFoldersList.layoutManager as GridLayoutManager).spanCount = 4
                                        (binding.fragmentImageFoldersList.adapter as ImageFoldersAdapter).run {
                                            submitList(currentList)
                                        }
                                    }
                                }
                            } else {
                                if((binding.fragmentImageFoldersList.layoutManager as GridLayoutManager).spanCount != 2) {
                                    UIManager.setImageFoldersGridSpanNumberPortrait(activity, 2)
                                    viewModel.MainScope?.launch {
                                        (binding.fragmentImageFoldersList.layoutManager as GridLayoutManager).spanCount = 2
                                        (binding.fragmentImageFoldersList.adapter as ImageFoldersAdapter).run {
                                            submitList(currentList)
                                        }
                                    }
                                }
                            }
                        } else {
                            if (activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                if((binding.fragmentImageFoldersList.layoutManager as GridLayoutManager).spanCount != 5) {
                                    UIManager.setImageFoldersGridSpanNumberLandscape(activity, 5)
                                    viewModel.MainScope?.launch {
                                        (binding.fragmentImageFoldersList.layoutManager as GridLayoutManager).spanCount = 5
                                        (binding.fragmentImageFoldersList.adapter as ImageFoldersAdapter).run {
                                            submitList(currentList)
                                        }
                                    }
                                }
                            } else {
                                if((binding.fragmentImageFoldersList.layoutManager as GridLayoutManager).spanCount != 3) {
                                    UIManager.setImageFoldersGridSpanNumberPortrait(activity, 3)
                                    viewModel.MainScope?.launch {
                                        (binding.fragmentImageFoldersList.layoutManager as GridLayoutManager).spanCount = 3
                                        (binding.fragmentImageFoldersList.adapter as ImageFoldersAdapter).run {
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
    private fun initList(imageFolderItems: MutableList<ImageFolderItem>) {
        binding.fragmentImageFoldersList.layoutManager = GridLayoutManager(context, UIManager.getImageFoldersGridSpanNumber(requireActivity()))

        ApplicationLoader.transientParcelables[UIManager.KEY_TRANSIENT_PARCELABLE_IMAGE_FOLDERS_MAIN_LIST_RV_STATE].let {
            if(it != null) binding.fragmentImageFoldersList.layoutManager?.onRestoreInstanceState(it)
        }

        binding.fragmentImageFoldersList.adapter = ImageFoldersAdapter(requireActivity(), imageFolderItems, layoutInflater, this@ImageFoldersFragment)

        binding.fragmentImageFoldersList.itemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean = true
        }

        (binding.fragmentImageFoldersList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        binding.fragmentImageFoldersList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dx > 0 || dy > 0) {
                    if(this@ImageFoldersFragment::viewModel.isInitialized) {
                        if(tabsBarVisible) {
                            if (!viewModel.selectionTool.selectionMode) {
                                binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout.animate().alpha(0f).setDuration(300).start()
                                activity.handler.postDelayed({
                                    binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.GONE
                                }, 300)
                            }
                            tabsBarVisible = false
                        }

                        if(toolBarVisible) {
                            binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolBarFoldersRootLayout.animate().alpha(0f).setDuration(300).start()
                            activity.handler.postDelayed({
                                binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolBarFoldersRootLayout.visibility = View.GONE
                            }, 300)
                            toolBarVisible = false
                        }

                        if(scrollBtnVisible) {
                            binding.fragmentImageFoldersScrollBtnIcon.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)

                            binding.fragmentImageFoldersScrollBtn.setOnClickListener {
                                binding.fragmentImageFoldersList.scrollToPosition(if(binding.fragmentImageFoldersList.adapter!!.itemCount > 0) binding.fragmentImageFoldersList.adapter!!.itemCount - 1 else 0)
                            }
                        }
                    }
                } else {
                    if (this@ImageFoldersFragment::viewModel.isInitialized) {
                        if(!viewModel.selectionTool.selectionMode) binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.VISIBLE
                        if(viewModel.selectionTool.selectionMode) binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolBarFoldersRootLayout.visibility = View.VISIBLE

                        if(!tabsBarVisible) {
                            if (!viewModel.selectionTool.selectionMode) binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout.animate().alpha(1f).setDuration(300).start()
                            tabsBarVisible = true
                        }

                        if(!toolBarVisible) {
                            binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolBarFoldersRootLayout.animate().alpha(1f).setDuration(300).start()
                            toolBarVisible = true
                        }

                        if(scrollBtnVisible) {
                            binding.fragmentImageFoldersScrollBtnIcon.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)

                            binding.fragmentImageFoldersScrollBtn.setOnClickListener {
                                binding.fragmentImageFoldersList.scrollToPosition(0)
                            }
                        }
                    }
                }
            }
        })

        binding.fragmentImageFoldersList.setOnTouchListener { _, event ->
            pinchZoomGestureDetector.onTouchEvent(event)
            false
        }

        notifyListEmpty(imageFolderItems.size, binding.fragmentImageFoldersNoFoldersTitle, binding.fragmentImageFoldersScrollBtn)
    }

    override fun launchCore() {
        activity.requestExternalStoragePermission {
            ApplicationLoader.ApplicationIOScope.launch {
                viewModel = ViewModelProvider(this@ImageFoldersFragment, SimpleInjector.provideImageFoldersViewModelFactory(frContext)).get(ImageFoldersViewModel::class.java)

                withContext(Main) {
                    viewModel.librarySortOrder = PreferencesWrapper.getString(frContext, SortTool.KEY_SP_IMAGE_LIBRARY_SORT_ORDER, SortTool.SORT_ORDER_DATE_RECENT)
                    viewModel.getItemsLive(frContext).observe(viewLifecycleOwner, this@ImageFoldersFragment)

                    initList(viewModel.getItemsLive(frContext).value!!)

                    binding.fragmentImageFoldersListProgressBar.visibility = View.GONE

                    if(viewModel.contentObserver == null) viewModel.contentObserver = BaseContentObserver(frContext, viewModel, activity.handler)
                    frContext.contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, viewModel.contentObserver!!)

                    initSelectionState(
                            viewModel,
                            activity,
                            binding.fragmentImageFoldersList.adapter!!,
                            binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolBarFoldersRootLayout,
                            binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                            binding.fragmentImageFoldersAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                            binding.fragmentImageFoldersAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb
                    )

                    initSelectionBar(
                            binding.fragmentImageFoldersAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb
                    ) { _: CompoundButton, b: Boolean ->
                        if(!viewModel.selectionTool.selectionCheckBoxSticky) {
                            viewModel.MainScope?.launch {
                                if(b) {
                                    viewModel.selectionTool.selectAll(mutableListOf<String>().apply {
                                        if(binding.fragmentImageFoldersList.adapter != null) {
                                            (binding.fragmentImageFoldersList.adapter as ImageFoldersAdapter).currentList.forEach {
                                                add(it.data)
                                            }
                                        }
                                    }, binding.fragmentImageFoldersList.adapter!!)
                                } else {
                                    viewModel.selectionTool.unselectAll(binding.fragmentImageFoldersList.adapter!!)
                                }
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
                                onBackCallback.isEnabled = false
                                if(this@ImageFoldersFragment::searchBackCallback.isInitialized) searchBackCallback.isEnabled = false
                                activity.onBackPressed()
                            },
                            binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarFoldersTitleContainer,
                            binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarFoldersTitle,
                            {

                            },
                    )

                    initToolBar(
                            binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolBarFoldersRootLayout,
                            mutableListOf<ViewGroup>().apply {
                                add(binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolBarFoldersInfoContainer)
                            },
                            mutableListOf<View.OnClickListener>().apply {
                                add {
                                    if(viewModel.selectionTool.selectedPaths.isNotEmpty()) {
                                        InfoTool.showInfoAlbumBottomModalSheetFragment(
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
                            },
                            mutableListOf<TextView>().apply {
                                add(binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolBarFoldersInfoTitle)
                            }
                    )
                }

            }
        }
    }

    override fun onResume() {
        super.onResume()

        activity.supportActionBar?.title = frContext.resources.getString(R.string.title_folders)

        handleUserReturnFromAppSettings(activity)
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

        searchView.post {
            searchView.apply {
                searchView.queryHint = frContext.resources.getString(R.string.title_search_hint)

                imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI

                setOnSearchClickListener {
                    if(this@ImageFoldersFragment::viewModel.isInitialized)
                        viewModel.isSearchViewEnabled = true
                }

                setOnCloseListener {
                    if(this@ImageFoldersFragment::viewModel.isInitialized)
                        viewModel.isSearchViewEnabled = false

                    searchBackCallback.isEnabled = false

                    false
                }

                if(this@ImageFoldersFragment::viewModel.isInitialized) {
                    ApplicationLoader.transientStrings[UIManager.KEY_TRANSIENT_STRINGS_IMAGE_FOLDERS_SEARCH_TEXT].let {
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

                        ApplicationLoader.transientStrings.remove(UIManager.KEY_TRANSIENT_STRINGS_IMAGE_FOLDERS_SEARCH_TEXT)
                    }
                }

                setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        if(this@ImageFoldersFragment::viewModel.isInitialized) {
                            viewModel.IOScope.launch {
                                viewModel.setSearchText(newText)
                                viewModel.assignItemsLive(frContext, false)
                            }

                            searchBackCallback.isEnabled = !newText.isNullOrEmpty()
                        }

                        return false
                    }
                })
            }
        }

        menu.findItem(R.id.mainToolbarMenuItemSort).setOnMenuItemClickListener {
            if(this@ImageFoldersFragment::viewModel.isInitialized && !SortTool.showingDialogInProgress) {
                SortTool.showSortBottomModalSheetFragment(activity.supportFragmentManager, viewModel)
            }

            true
        }

    }

    override fun onPause() {
        super.onPause()

        if(this::viewModel.isInitialized) {
            if(this::searchView.isInitialized)
                if(viewModel.isSearchViewEnabled) ApplicationLoader.transientStrings[UIManager.KEY_TRANSIENT_STRINGS_IMAGE_FOLDERS_SEARCH_TEXT] = searchView.query.toString()

            ApplicationLoader.transientParcelables[UIManager.KEY_TRANSIENT_PARCELABLE_IMAGE_FOLDERS_MAIN_LIST_RV_STATE] = binding.fragmentImageFoldersList.layoutManager?.onSaveInstanceState()
        }

        viewModelStore.clear()
    }
}