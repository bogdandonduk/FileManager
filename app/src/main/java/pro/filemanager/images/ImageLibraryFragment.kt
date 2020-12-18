package pro.filemanager.images

import android.os.Bundle
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
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.android.synthetic.main.layout_bottom_tabs_bar.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import pro.filemanager.ApplicationLoader
import pro.filemanager.R
import pro.filemanager.core.PreferencesWrapper
import pro.filemanager.core.SimpleInjector
import pro.filemanager.core.UIManager
import pro.filemanager.core.base.BaseItem
import pro.filemanager.core.base.BaseSectionFragment
import pro.filemanager.core.tools.DeleteTool
import pro.filemanager.core.tools.ShareTool
import pro.filemanager.core.tools.info.InfoTool
import pro.filemanager.core.tools.rename.RenameTool
import pro.filemanager.core.tools.sort.SortTool
import pro.filemanager.databinding.FragmentImageLibraryBinding
import pro.filemanager.files.FileCore
import pro.filemanager.images.folders.ImageFolderItem

class ImageLibraryFragment : BaseSectionFragment(), Observer<MutableList<ImageItem>> {

    lateinit var binding: FragmentImageLibraryBinding
    lateinit var viewModel: ImageLibraryViewModel

    var folderItem: ImageFolderItem? = null

    lateinit var onBackCallback: OnBackPressedCallback
    lateinit var searchBackCallback: OnBackPressedCallback

    lateinit var searchView: SearchView

    override fun onChanged(t: MutableList<ImageItem>?) {
        if(binding.fragmentImageLibraryList.adapter != null) {
            try {
                viewModel.MainScope?.cancel()
                viewModel.MainScope = CoroutineScope(Main)
            } catch(thr: Throwable) {

            }

           if(shouldUseDiffUtil) {
               (binding.fragmentImageLibraryList.adapter as ImageLibraryAdapter).submitItems(t!!)
           } else {
               (binding.fragmentImageLibraryList.adapter as ImageLibraryAdapter).submitItemsWithoutDiff(t!!)
           }

           if(shouldScrollToTop) binding.fragmentImageLibraryList.scrollToPosition(0)

            shouldScrollToTop = true
            shouldUseDiffUtil = false
            viewModel.searchInProgress = false

            notifyListEmpty(binding.fragmentImageLibraryList.adapter!!, binding.fragmentImageLibraryNoImagesTitle)
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

            folderItem = arguments?.getParcelable(FileCore.KEY_ARGUMENT_ALBUM_PARCELABLE)

            setHasOptionsMenu(true)

            activity.setSupportActionBar(binding.fragmentImageLibraryAppBarInclude.layoutBaseToolBarInclude.layoutBaseToolbar)

            activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(folderItem == null) {
                override fun handleOnBackPressed() {
                    navController.popBackStack(R.id.homeFragment, false)
                }
            }.apply {
                onBackCallback = this
            })

            launchCore()
        } catch(thr: Throwable) {

        }

    }


    private fun initList(imageItems: MutableList<ImageItem>) {
        binding.fragmentImageLibraryList.layoutManager = GridLayoutManager(context, UIManager.getItemGridSpanNumber(requireActivity()))

        binding.fragmentImageLibraryList.layoutManager?.onRestoreInstanceState(viewModel.mainListRvState)

        viewModel.mainListRvState = null

        binding.fragmentImageLibraryList.adapter = ImageLibraryAdapter(requireActivity(), imageItems, layoutInflater, this@ImageLibraryFragment)

        binding.fragmentImageLibraryList.itemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean = true
        }

        (binding.fragmentImageLibraryList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        binding.fragmentImageLibraryList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dx > 0 || dy > 0) {
                    if(this@ImageLibraryFragment::viewModel.isInitialized && !viewModel.selectionTool.selectionMode)
                        binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.GONE
                } else {
                    if(this@ImageLibraryFragment::viewModel.isInitialized && !viewModel.selectionTool.selectionMode)
                        binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.VISIBLE
                }
            }
        })

        notifyListEmpty(binding.fragmentImageLibraryList.adapter!!, binding.fragmentImageLibraryNoImagesTitle)
    }

    override fun launchCore() {
        activity.requestExternalStoragePermission {
            ApplicationLoader.ApplicationIOScope.launch {
                viewModel = ViewModelProvider(this@ImageLibraryFragment, SimpleInjector.provideImageLibraryViewModelFactory(folderItem)).get(ImageLibraryViewModel::class.java)

                withContext(Main) {
                    viewModel.getItemsLive(frContext).observe(viewLifecycleOwner, this@ImageLibraryFragment)

                    initList(viewModel.getItemsLive(frContext).value!!)

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
                        viewModel.MainScope?.launch {
                            if(b) {
                                viewModel.selectionTool.selectAll(mutableListOf<String>().apply {
                                    if (binding.fragmentImageLibraryList.adapter != null) {
                                        (binding.fragmentImageLibraryList.adapter as ImageLibraryAdapter).imageItems.forEach {
                                            add(it.data)
                                        }
                                    }
                                }, binding.fragmentImageLibraryList.adapter!!)
                            } else {
                                viewModel.selectionTool.unselectAll(binding.fragmentImageLibraryList.adapter!!)
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
                                    activity.onBackPressed()
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
                                    navController.navigate(R.id.action_imageLibraryFragment_to_imageFoldersFragment,
                                            null,
                                            if(this@ImageLibraryFragment.folderItem != null)
                                                NavOptions.Builder()
                                                        .setEnterAnim(R.anim.fragment_close_enter)
                                                        .build()
                                            else null
                                    )
                                }
                            }
                    )

                    initToolBar(
                            binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarRootLayout,
                            mutableListOf<ViewGroup>().apply {
                                            add(binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarShareContainer)
                                            add(binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarMoveContainer)
                                            add(binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarCopyContainer)
                                            add(binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarRenameContainer)
                                            add(binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarInfoContainer)
                                            add(binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarDeleteContainer)
                            },
                            mutableListOf<View.OnClickListener>().apply {
                                add {
                                    if(viewModel.selectionTool.selectedPaths.isNotEmpty()) {
                                        ApplicationLoader.ApplicationMainScope.launch {
                                            ShareTool.shareImages(frContext, viewModel.selectionTool.selectedPaths)
                                        }
                                    }
                                }
                                add {
                                    stabilizingToast.show()
                                }
                                add {
                                    stabilizingToast.show()
                                }
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
                                        InfoTool.showInfoItemBottomModalSheetFragment(
                                                activity.supportFragmentManager,
                                                mutableListOf<BaseItem>().apply {
                                                    (binding.fragmentImageLibraryList.adapter as ImageLibraryAdapter).imageItems.forEach {
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
                                                DeleteTool.deleteItemsAndRefreshMediaStore(activity, viewModel.selectionTool.selectedPaths) {
                                                    shouldUseDiffUtil = true
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
                                add(binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarShareTitle)
                                add(binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarMoveTitle)
                                add(binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarCopyTitle)
                                add(binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarRenameTitle)
                                add(binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarInfoTitle)
                                add(binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarDeleteTitle)
                            }
                    )

                    if(DeleteTool.inProgressDialogCode == 1) {
                        binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarDeleteContainer.callOnClick()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        setAppBarTitle(activity, folderItem?.displayName ?: frContext.resources.getString(R.string.title_images))

        handleUserReturnFromAppSettings(activity)

        updateSortOrder()
    }

    private fun updateSortOrder() {
        if(this::viewModel.isInitialized && viewModel.currentSortOrder != PreferencesWrapper.getString(frContext, SortTool.KEY_SP_IMAGE_LIBRARY_SORT_ORDER, SortTool.SORT_ORDER_DATE_RECENT))
            ApplicationLoader.ApplicationIOScope.launch {
                viewModel.currentSortOrder = PreferencesWrapper.getString(frContext, SortTool.KEY_SP_IMAGE_LIBRARY_SORT_ORDER, SortTool.SORT_ORDER_DATE_RECENT)

                shouldScrollToTop = true

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
                ApplicationLoader.transientStrings[UIManager.KEY_TRANSIENT_STRINGS_LIBRARY_SEARCH_TEXT].let {
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

                    ApplicationLoader.transientStrings.remove(UIManager.KEY_TRANSIENT_STRINGS_LIBRARY_SEARCH_TEXT)
                }
            }

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if(this@ImageLibraryFragment::viewModel.isInitialized) {
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

        menu.findItem(R.id.mainToolbarMenuItemSort).setOnMenuItemClickListener {
            if(this@ImageLibraryFragment::viewModel.isInitialized) {
                SortTool.showSortBottomModalSheetFragment(activity.supportFragmentManager, viewModel)
            }

            true
        }

        menu.findItem(R.id.mainToolbarMenuItemEdit).setOnMenuItemClickListener {
            if(this@ImageLibraryFragment::viewModel.isInitialized && binding.fragmentImageLibraryList.adapter != null) {
                menu.close()

                viewModel.selectionTool.selectionMode = true

                viewModel.selectionTool.unselectAll(binding.fragmentImageLibraryList.adapter!!)

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

            true
        }
    }

    override fun onStop() {
        super.onStop()

        if(this::viewModel.isInitialized) {
            if(this::searchView.isInitialized && folderItem == null)
                if(viewModel.isSearchViewEnabled) ApplicationLoader.transientStrings[UIManager.KEY_TRANSIENT_STRINGS_LIBRARY_SEARCH_TEXT] = searchView.query.toString()
                    viewModel.mainListRvState = binding.fragmentImageLibraryList.layoutManager?.onSaveInstanceState()
        }
    }
}