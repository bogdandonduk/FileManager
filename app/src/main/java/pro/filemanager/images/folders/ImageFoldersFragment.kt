package pro.filemanager.images.folders

import android.os.Bundle
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
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import pro.filemanager.ApplicationLoader
import pro.filemanager.R
import pro.filemanager.core.*
import pro.filemanager.core.base.BaseFolderItem
import pro.filemanager.core.base.BaseSectionFragment
import pro.filemanager.core.tools.DeleteTool
import pro.filemanager.core.tools.info.InfoTool
import pro.filemanager.core.tools.rename.RenameTool
import pro.filemanager.core.tools.sort.SortTool
import pro.filemanager.databinding.FragmentImageFoldersBinding
import pro.filemanager.images.ImageRepo
import pro.filemanager.images.folders.ImageFolderItem
import pro.filemanager.images.folders.ImageFoldersAdapter
import pro.filemanager.images.folders.ImageFoldersViewModel

class ImageFoldersFragment : BaseSectionFragment(), Observer<MutableList<ImageFolderItem>> {

    lateinit var binding: FragmentImageFoldersBinding
    lateinit var viewModel: ImageFoldersViewModel

    lateinit var onBackCallback: OnBackPressedCallback
    lateinit var searchBackCallback: OnBackPressedCallback

    lateinit var searchView: SearchView

    override fun onChanged(t: MutableList<ImageFolderItem>?) {
        if(binding.fragmentImageFoldersList.adapter != null) {
            try {
                viewModel.MainScope?.cancel()
                viewModel.MainScope = CoroutineScope(Main)
            } catch(thr: Throwable) {

            }

            if(shouldUseDiffUtil) {
                (binding.fragmentImageFoldersList.adapter as ImageFoldersAdapter).submitItems(t!!)
            } else {
                (binding.fragmentImageFoldersList.adapter as ImageFoldersAdapter).submitItemsWithoutDiff(t!!)
            }

            if(shouldScrollToTop) binding.fragmentImageFoldersList.scrollToPosition(0)

            shouldScrollToTop = true
            shouldUseDiffUtil = false
            viewModel.searchInProgress = false

            notifyListEmpty(binding.fragmentImageFoldersList.adapter!!, binding.fragmentImageFoldersNoFoldersTitle)
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

            activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(ApplicationLoader.folderFragmentImmediateAction == null) {
                override fun handleOnBackPressed() {
                    navController.popBackStack(R.id.homeFragment, false)
                }
            }.apply {
                onBackCallback = this
            })

            ApplicationLoader.folderFragmentImmediateAction?.run() ?: launchCore()
            onBackCallback.isEnabled = true
        } catch(thr: Throwable) {

        }
    }

    private fun initList(imageFolderItems: MutableList<ImageFolderItem>) {
        binding.fragmentImageFoldersList.layoutManager = GridLayoutManager(context, UIManager.getAlbumGridSpanNumber(requireActivity()))

        ApplicationLoader.transientParcelables[UIManager.KEY_TRANSIENT_PARCELABLE_FOLDERS_MAIN_LIST_RV_STATE].let {
            if(it != null) {
                binding.fragmentImageFoldersList.layoutManager?.onRestoreInstanceState(it)
                ApplicationLoader.transientParcelables.remove(UIManager.KEY_TRANSIENT_PARCELABLE_FOLDERS_MAIN_LIST_RV_STATE)
            } else
                binding.fragmentImageFoldersList.layoutManager?.onRestoreInstanceState(viewModel.mainListRvState)
        }

        binding.fragmentImageFoldersList.adapter = ImageFoldersAdapter(requireActivity(), imageFolderItems, layoutInflater, this@ImageFoldersFragment)

        binding.fragmentImageFoldersList.itemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean = true
        }

        (binding.fragmentImageFoldersList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        binding.fragmentImageFoldersList.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dx > 0 || dy > 0) {
                    if(this@ImageFoldersFragment::viewModel.isInitialized && !viewModel.selectionTool.selectionMode)
                        binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.GONE
                } else {
                    if(this@ImageFoldersFragment::viewModel.isInitialized && !viewModel.selectionTool.selectionMode)
                        binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.VISIBLE
                }
            }
        })

        notifyListEmpty(binding.fragmentImageFoldersList.adapter!!, binding.fragmentImageFoldersNoFoldersTitle)
    }

    override fun launchCore() {
        activity.requestExternalStoragePermission {
            ApplicationLoader.ApplicationIOScope.launch {
                viewModel = ViewModelProvider(this@ImageFoldersFragment, SimpleInjector.provideImageFoldersViewModelFactory()).get(ImageFoldersViewModel::class.java)

                withContext(Main) {
                    viewModel.getItemsLive().observe(viewLifecycleOwner, this@ImageFoldersFragment)

                    initList(viewModel.getItemsLive().value!!)

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
                        viewModel.MainScope?.launch {
                            if(b) {
                                viewModel.selectionTool.selectAll(mutableListOf<String>().apply {
                                    if(binding.fragmentImageFoldersList.adapter != null) {
                                        (binding.fragmentImageFoldersList.adapter as ImageFoldersAdapter).imageFolderItems.forEach {
                                            add(it.data)
                                        }
                                    }
                                }, binding.fragmentImageFoldersList.adapter!!)
                            } else {
                                viewModel.selectionTool.unselectAll(binding.fragmentImageFoldersList.adapter!!)
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
                                add(binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolBarFoldersRenameContainer)
                                add(binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolBarFoldersInfoContainer)
                                add(binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolBarFoldersDeleteContainer)
                            },
                            mutableListOf<View.OnClickListener>().apply {
                                add {
                                    if(viewModel.selectionTool.selectedPaths.size == 1) {
                                        RenameTool.showRenameBottomModalSheetFragment(activity.supportFragmentManager, viewModel.selectionTool.selectedPaths[0]) {
                                            try {
                                                shouldUseDiffUtil = true
                                                shouldScrollToTop = false
                                                ApplicationLoader.ApplicationIOScope.launch {
                                                    viewModel.assignItemsLive(frContext, true)
                                                    ImageRepo.getSingleton().loadAll(frContext, true)
                                                }
                                            } catch(thr: Throwable) {

                                            }
                                        }
                                    }
                                }
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
                                add {
                                    if(viewModel.selectionTool.selectedPaths.isNotEmpty()) {
                                        try {
                                            ApplicationLoader.ApplicationMainScope.launch {
                                                DeleteTool.deleteFoldersAndRefreshMediaStore(
                                                        activity,
                                                        mutableListOf<String>().apply {
                                                            viewModel.selectionTool.selectedPaths.forEach { path ->
                                                                (binding.fragmentImageFoldersList.adapter as ImageFoldersAdapter).imageFolderItems.forEach { folder ->
                                                                    if(folder.data == path)
                                                                        folder.containedImages.forEach {
                                                                            add(it.data)
                                                                        }
                                                                }
                                                            }
                                                        },
                                                        viewModel.selectionTool.selectedPaths.size
                                                ) {
                                                    shouldScrollToTop = false

                                                    ApplicationLoader.ApplicationIOScope.launch {
                                                        viewModel.assignItemsLive(frContext, true)
                                                        ImageRepo.getSingleton().loadAll(frContext, true)
                                                    }
                                                }
                                            }
                                        } catch(thr: Throwable) {

                                        }
                                    }
                                }
                            },
                            mutableListOf<TextView>().apply {
                                add(binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolBarFoldersRenameTitle)
                                add(binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolBarFoldersInfoTitle)
                                add(binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolBarFoldersDeleteTitle)
                            }
                    )

                    if(DeleteTool.inProgressDialogCode == 2) {
                        binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolBarFoldersDeleteContainer.callOnClick()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        setAppBarTitle(activity, frContext.resources.getString(R.string.title_folders))

        handleUserReturnFromAppSettings(activity)

        updateAlbumCovers()
    }

    private fun updateAlbumCovers() {
        if(this::viewModel.isInitialized && viewModel.librarySortOrder != PreferencesWrapper.getString(frContext, SortTool.KEY_SP_IMAGE_LIBRARY_SORT_ORDER, SortTool.SORT_ORDER_DATE_RECENT))
            ApplicationLoader.ApplicationIOScope.launch {
                viewModel.librarySortOrder = PreferencesWrapper.getString(frContext, SortTool.KEY_SP_IMAGE_LIBRARY_SORT_ORDER, SortTool.SORT_ORDER_DATE_RECENT)

                shouldScrollToTop = false

                viewModel.assignItemsLive(frContext, false)
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
                    ApplicationLoader.transientStrings[UIManager.KEY_TRANSIENT_STRINGS_FOLDERS_SEARCH_TEXT].let {
                        if(it != null) {
                            viewModel.isSearchViewEnabled = true
                            viewModel.setSearchText(it)

                            if(viewModel.currentSearchText.isBlank()) viewModel.setSearchText(null)

                            if(viewModel.currentSearchText.isNotEmpty())
                                viewModel.IOScope.launch {
                                    shouldScrollToTop = false

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

                        ApplicationLoader.transientStrings.remove(UIManager.KEY_TRANSIENT_STRINGS_FOLDERS_SEARCH_TEXT)
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

        menu.findItem(R.id.mainToolbarMenuItemEdit).setOnMenuItemClickListener {
            if(this@ImageFoldersFragment::viewModel.isInitialized && binding.fragmentImageFoldersList.adapter != null) {
                menu.close()

                viewModel.selectionTool.selectionMode = true

                viewModel.selectionTool.unselectAll(binding.fragmentImageFoldersList.adapter!!)

                initSelectionState(
                        viewModel,
                        activity,
                        binding.fragmentImageFoldersList.adapter!!,
                        binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolBarFoldersRootLayout,
                        binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                        binding.fragmentImageFoldersAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                        binding.fragmentImageFoldersAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb
                )
            }

            true
        }

    }

    override fun onPause() {
        super.onPause()

        if(this::viewModel.isInitialized) {
            if(this::searchView.isInitialized)
                if(viewModel.isSearchViewEnabled) ApplicationLoader.transientStrings[UIManager.KEY_TRANSIENT_STRINGS_FOLDERS_SEARCH_TEXT] = searchView.query.toString()

            ApplicationLoader.transientParcelables[UIManager.KEY_TRANSIENT_PARCELABLE_FOLDERS_MAIN_LIST_RV_STATE] = binding.fragmentImageFoldersList.layoutManager?.onSaveInstanceState()
        }
    }
}