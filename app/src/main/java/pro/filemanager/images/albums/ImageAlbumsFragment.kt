package pro.filemanager.images.albums

import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity
import pro.filemanager.R
import pro.filemanager.core.*
import pro.filemanager.core.base.BaseAlbumItem
import pro.filemanager.core.base.BaseFragment
import pro.filemanager.core.base.BaseItem
import pro.filemanager.core.tools.DeleteTool
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.core.tools.info.InfoTool
import pro.filemanager.core.tools.sort.SortTool
import pro.filemanager.databinding.FragmentImageAlbumsBinding
import pro.filemanager.images.ImageBrowserAdapter

class ImageAlbumsFragment : BaseFragment(), Observer<MutableList<ImageAlbumItem>> {

    lateinit var binding: FragmentImageAlbumsBinding
    lateinit var viewModel: ImageAlbumsViewModel

    lateinit var onBackCallback: OnBackPressedCallback
    lateinit var searchView: SearchView

    var shouldScrollToTop = true

    override fun onChanged(t: MutableList<ImageAlbumItem>?) {
        if(binding.fragmentImageAlbumsList.adapter  != null) {
            try {
                viewModel.MainScope?.cancel()
                viewModel.MainScope = null
                viewModel.MainScope = CoroutineScope(Main)
            } catch(thr: Throwable) {

            }

            (binding.fragmentImageAlbumsList.adapter as ImageAlbumsAdapter).imageAlbumItems = t!!
            binding.fragmentImageAlbumsList.adapter!!.notifyDataSetChanged()

            if(shouldScrollToTop) binding.fragmentImageAlbumsList.scrollToPosition(0)

            shouldScrollToTop = true

            viewModel.searchInProgress = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageAlbumsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(binding.root)

        activity = requireActivity() as HomeActivity

        setHasOptionsMenu(true)

        activity.setSupportActionBar(binding.fragmentImageAlbumsToolbarInclude.layoutBaseToolBarInclude.layoutBaseToolbar)

        onBackCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navController.popBackStack(R.id.homeFragment, false)
            }
        }

        activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackCallback)

        launchCore()

        binding.fragmentImageAlbumsBottomTabsBarInclude.layoutBottomTabsBarRootLayout.post {
            binding.fragmentImageAlbumsBottomTabsBarInclude.layoutBottomTabsBarRootLayout.height.let {
                binding.fragmentImageAlbumsBottomTabsBarInclude.layoutBottomBarGalleryTitle.textSize = (it / 8).toFloat()
                binding.fragmentImageAlbumsBottomTabsBarInclude.layoutBottomBarGalleryTitle.text = resources.getText(R.string.title_gallery)

                binding.fragmentImageAlbumsBottomTabsBarInclude.layoutBottomTabsBarAlbumsTitle.textSize = (it / 8).toFloat()
                binding.fragmentImageAlbumsBottomTabsBarInclude.layoutBottomTabsBarAlbumsTitle.text = resources.getText(R.string.title_folders)
            }
        }

        binding.fragmentImageAlbumsBottomTabsBarInclude.layoutBottomTabsBarAlbumsTitle.setTypeface(null, Typeface.BOLD)
        binding.fragmentImageAlbumsBottomTabsBarInclude.layoutBottomTabsBarAlbumsTitleIndicator.visibility = View.VISIBLE

        binding.fragmentImageAlbumsBottomTabsBarInclude.layoutBottomTabsBarGalleryTitleContainer.setOnClickListener {
            onBackCallback.isEnabled = false
            ApplicationLoader.transientParcelables[UIManager.KEY_TRANSIENT_PARCELABLE_ALBUMS_MAIN_LIST_RV_STATE] = binding.fragmentImageAlbumsList.layoutManager?.onSaveInstanceState()
            activity.onBackPressed()
        }

    }

    private fun initAdapter(audioAlbumItems: MutableList<ImageAlbumItem>) {
        binding.fragmentImageAlbumsList.layoutManager = GridLayoutManager(context, UIManager.getAlbumGridSpanNumber(requireActivity()))

        ApplicationLoader.transientParcelables[UIManager.KEY_TRANSIENT_PARCELABLE_ALBUMS_MAIN_LIST_RV_STATE].let {
            if(it != null) {
                binding.fragmentImageAlbumsList.layoutManager?.onRestoreInstanceState(it)
                ApplicationLoader.transientParcelables.remove(UIManager.KEY_TRANSIENT_PARCELABLE_ALBUMS_MAIN_LIST_RV_STATE)
            } else
                binding.fragmentImageAlbumsList.layoutManager?.onRestoreInstanceState(viewModel.mainListRvState)
        }

        binding.fragmentImageAlbumsList.adapter = ImageAlbumsAdapter(requireActivity(), audioAlbumItems, layoutInflater, this@ImageAlbumsFragment)

        binding.fragmentImageAlbumsList.itemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean = true
        }

        binding.fragmentImageAlbumsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dx > 0 || dy > 0) {
                    if(this@ImageAlbumsFragment::viewModel.isInitialized && viewModel.selectionTool != null && !viewModel.selectionTool!!.selectionMode)
                        binding.fragmentImageAlbumsBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.GONE
                } else {
                    if(this@ImageAlbumsFragment::viewModel.isInitialized && viewModel.selectionTool != null && !viewModel.selectionTool!!.selectionMode)
                        binding.fragmentImageAlbumsBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.VISIBLE
                }
            }
        })
    }

    fun launchCore() {
        activity.requestExternalStoragePermission {
            ApplicationLoader.ApplicationIOScope.launch {
                viewModel = ViewModelProviders.of(this@ImageAlbumsFragment, SimpleInjector.provideImageAlbumsViewModelFactory()).get(ImageAlbumsViewModel::class.java)

                withContext(Main) {
                    viewModel.getAlbumsLive().observe(viewLifecycleOwner, this@ImageAlbumsFragment)

                    try {
                        initAdapter(viewModel.getAlbumsLive().value!!)

                        ApplicationLoader.transientStrings[UIManager.KEY_TRANSIENT_STRINGS_ALBUMS_SEARCH_TEXT].let {
                            if(!it.isNullOrEmpty()) {
                                viewModel.isSearchViewEnabled = true
                                viewModel.setSearchText(it)

                                viewModel.assignItemsLive(frContext, false)
                            }
                        }

                        if(binding.fragmentImageAlbumsList.adapter!!.itemCount <= 0) {
                            viewModel.MainScope?.launch {
                                viewModel.selectionTool!!.initSelectionState(
                                        activity,
                                        binding.fragmentImageAlbumsList.adapter!!,
                                        binding.fragmentImageAlbumsBottomToolBarInclude.layoutBottomToolBarAlbumRootLayout,
                                        binding.fragmentImageAlbumsBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                                        binding.fragmentImageAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                                        binding.fragmentImageAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb,
                                        viewModel.selectionTool!!.selectionMode,
                                        viewModel.selectionTool!!.selectedPaths.size
                                )
                            }
                        }

                        if(viewModel.selectionTool == null) viewModel.selectionTool = SelectionTool()

                        if(binding.fragmentImageAlbumsList.adapter!!.itemCount > 0) {
                            for (i in 0 until binding.fragmentImageAlbumsList.adapter!!.itemCount) {
                                viewModel.MainScope?.launch {
                                    binding.fragmentImageAlbumsList.adapter!!.notifyItemChanged(i)
                                }
                            }
                        } else {
                            viewModel.MainScope?.launch {
                                viewModel.selectionTool!!.initSelectionState(
                                        activity,
                                        binding.fragmentImageAlbumsList.adapter!!,
                                        binding.fragmentImageAlbumsBottomToolBarInclude.layoutBottomToolBarAlbumRootLayout,
                                        binding.fragmentImageAlbumsBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                                        binding.fragmentImageAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                                        binding.fragmentImageAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb,
                                        viewModel.selectionTool!!.selectionMode,
                                        viewModel.selectionTool!!.selectedPaths.size
                                )
                            }
                        }

                        binding.fragmentImageAlbumsBottomToolBarInclude.layoutBottomToolBarAlbumRootLayout.post {
                            binding.fragmentImageAlbumsBottomToolBarInclude.layoutBottomToolBarAlbumRootLayout.height.let {
                                binding.fragmentImageAlbumsBottomToolBarInclude.layoutBottomToolBarAlbumInfoTitle.textSize = (it / 50).toFloat()
                                binding.fragmentImageAlbumsBottomToolBarInclude.layoutBottomToolBarAlbumDeleteTitle.textSize = (it / 50).toFloat()
                            }
                        }

                        binding.fragmentImageAlbumsBottomToolBarInclude.layoutBottomToolBarAlbumRootLayout.post {
                            binding.fragmentImageAlbumsBottomToolBarInclude.layoutBottomToolBarAlbumRootLayout.height.let {
                                binding.fragmentImageAlbumsBottomToolBarInclude.layoutBottomToolBarAlbumInfoTitle.textSize = (it / 10).toFloat()
                                binding.fragmentImageAlbumsBottomToolBarInclude.layoutBottomToolBarAlbumDeleteTitle.textSize = (it / 10).toFloat()
                            }

                            binding.fragmentImageAlbumsBottomToolBarInclude.layoutBottomToolBarAlbumRootLayout.visibility = View.GONE
                        }

                        if (viewModel.selectionTool!!.selectionMode) {
                            if (viewModel.selectionTool!!.selectedPaths.isNotEmpty()) {
                                activity.supportActionBar?.hide()
                                binding.fragmentImageAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout.visibility = View.VISIBLE
                                binding.fragmentImageAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb.text = viewModel.selectionTool!!.selectedPaths.size.toString()
                            }

                            binding.fragmentImageAlbumsBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.GONE
                            binding.fragmentImageAlbumsBottomToolBarInclude.layoutBottomToolBarAlbumRootLayout.visibility = View.VISIBLE
                        }

                        binding.fragmentImageAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
                            if(this@ImageAlbumsFragment::viewModel.isInitialized && binding.fragmentImageAlbumsList.adapter != null) {
                                viewModel.MainScope?.launch {
                                    if(b) {
                                        viewModel.selectionTool!!.selectAll(mutableListOf<String>().apply {
                                            if(binding.fragmentImageAlbumsList.adapter != null) {
                                                (binding.fragmentImageAlbumsList.adapter as ImageBrowserAdapter).imageItems.forEach {
                                                    add(it.data)
                                                }
                                            }
                                        }, binding.fragmentImageAlbumsList.adapter!!, viewModel.MainScope)
                                    } else {
                                        viewModel.selectionTool!!.unselectAll(binding.fragmentImageAlbumsList.adapter!!, viewModel.MainScope)
                                    }
                                }
                            }
                        }

                        binding.fragmentImageAlbumsBottomToolBarInclude.layoutBottomToolBarAlbumInfoContainer.setOnClickListener {
                            if(this@ImageAlbumsFragment::viewModel.isInitialized &&
                                    !InfoTool.showingDialogInProgress &&
                                    viewModel.selectionTool != null &&
                                    !viewModel.selectionTool!!.selectedPaths.isNullOrEmpty() &&
                                    binding.fragmentImageAlbumsList.adapter != null
                            ) {
                                ApplicationLoader.ApplicationIOScope.launch {
                                    InfoTool.showInfoAlbumBottomModalSheetFragment(
                                            activity.supportFragmentManager,
                                            mutableListOf<BaseAlbumItem>().apply {
                                                (binding.fragmentImageAlbumsList.adapter as ImageAlbumsAdapter).imageAlbumItems.forEach {
                                                    if(viewModel.selectionTool!!.selectedPaths.contains(it.data)) {
                                                        add(it)
                                                    }
                                                }
                                            }
                                    )
                                }
                            }
                        }

                        binding.fragmentImageAlbumsBottomToolBarInclude.layoutBottomToolBarAlbumDeleteContainer.setOnClickListener {
                            try {
                                if(this@ImageAlbumsFragment::viewModel.isInitialized &&
                                        viewModel.selectionTool != null &&
                                        viewModel.selectionTool!!.selectionMode &&
                                        viewModel.selectionTool!!.selectedPaths.isNotEmpty() &&
                                        binding.fragmentImageAlbumsList.adapter != null
                                ) {
                                    ApplicationLoader.ApplicationMainScope.launch {
                                        DeleteTool.deleteAlbumsAndRefreshMediaStore(activity,
                                                mutableListOf<String>().apply {
                                                    viewModel.selectionTool!!.selectedPaths.forEach { path ->
                                                        (binding.fragmentImageAlbumsList.adapter as ImageAlbumsAdapter).imageAlbumItems.forEach { albumItem ->
                                                            if(albumItem.data == path) {
                                                                albumItem.containedItems.forEach {
                                                                    add(it.data)
                                                                }
                                                            }
                                                        }
                                                    }
                                                },
                                                viewModel.selectionTool!!.selectedPaths.size, viewModel) {
                                            ApplicationLoader.ApplicationIOScope.launch {
                                                shouldScrollToTop = false
                                                viewModel.assignItemsLive(frContext, true)
                                            }
                                        }
                                    }
                                }
                            } catch (thr: Throwable) {

                            }
                        }

                        viewModel.shownDialogs.forEach {
                            it.show()
                        }
                    } catch (thr: Throwable) {

                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        activity.supportActionBar?.title = requireContext().resources.getString(R.string.title_folders)

        if(ApplicationLoader.isUserSentToAppDetailsSettings && PermissionWrapper.checkExternalStoragePermissions(frContext)) {
            launchCore()
            ApplicationLoader.isUserSentToAppDetailsSettings = false
        } else if(ApplicationLoader.isUserSentToAppDetailsSettings && !PermissionWrapper.checkExternalStoragePermissions(frContext)) {
            ApplicationLoader.isUserSentToAppDetailsSettings = false
            activity.onBackPressed()
        }

        binding.fragmentImageAlbumsList.adapter.run {
            if(this != null) {
                (this as ImageAlbumsAdapter).imageAlbumItems.forEachIndexed { i: Int, _: ImageAlbumItem ->
                    this.notifyItemChanged(i)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_toolbar_menu, menu)

        searchView = menu.findItem(R.id.mainToolbarMenuItemSearch).actionView as SearchView

        searchView.post {
            searchView.apply {
                imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI

                setOnSearchClickListener {
                    if(this@ImageAlbumsFragment::viewModel.isInitialized)
                        viewModel.isSearchViewEnabled = true
                }

                setOnCloseListener {
                    if(this@ImageAlbumsFragment::viewModel.isInitialized)
                        viewModel.isSearchViewEnabled = false

                    ApplicationLoader.transientStrings.remove(UIManager.KEY_TRANSIENT_STRINGS_ALBUMS_SEARCH_TEXT)

                    false
                }

                if(this@ImageAlbumsFragment::viewModel.isInitialized && viewModel.isSearchViewEnabled) {
                    setQuery(viewModel.currentSearchText, false)
                    isIconified = false
                    requestFocus()

                    if(viewModel.currentSearchText.isEmpty()) clearFocus()
                } else {
                    isIconified = true
                }

                setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        if(this@ImageAlbumsFragment::viewModel.isInitialized) {
                            viewModel.IOScope.launch {
                                viewModel.setSearchText(newText)
                                viewModel.assignItemsLive(frContext, false)

                                ApplicationLoader.transientStrings[UIManager.KEY_TRANSIENT_STRINGS_ALBUMS_SEARCH_TEXT] = newText
                            }
                        }

                        return false
                    }

                })
            }
        }

        menu.findItem(R.id.mainToolbarMenuItemSort).setOnMenuItemClickListener {
            if(this@ImageAlbumsFragment::viewModel.isInitialized && !SortTool.showingDialogInProgress) {
                SortTool.showSortBottomModalSheetFragment(activity.supportFragmentManager, viewModel)
            }

            true
        }

        menu.findItem(R.id.mainToolbarMenuItemEdit).setOnMenuItemClickListener {
            if(this@ImageAlbumsFragment::viewModel.isInitialized && viewModel.selectionTool != null && binding.fragmentImageAlbumsList.adapter != null) {
                viewModel.selectionTool!!.selectionMode = true

                if(binding.fragmentImageAlbumsList.adapter!!.itemCount > 0) {
                    for (i in 0 until binding.fragmentImageAlbumsList.adapter!!.itemCount) {
                        viewModel.MainScope?.launch {
                            binding.fragmentImageAlbumsList.adapter!!.notifyItemChanged(i)
                        }
                    }
                } else {
                    viewModel.MainScope?.launch {
                        viewModel.selectionTool!!.initSelectionState(
                                activity,
                                binding.fragmentImageAlbumsList.adapter!!,
                                binding.fragmentImageAlbumsBottomToolBarInclude.layoutBottomToolBarAlbumRootLayout,
                                binding.fragmentImageAlbumsBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                                binding.fragmentImageAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                                binding.fragmentImageAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb,
                                viewModel.selectionTool!!.selectionMode,
                                viewModel.selectionTool!!.selectedPaths.size
                        )
                    }
                }
            }

            true
        }

    }

    override fun onStop() {
        super.onStop()

        if(this::viewModel.isInitialized)
            viewModel.mainListRvState = binding.fragmentImageAlbumsList.layoutManager?.onSaveInstanceState()

    }

}