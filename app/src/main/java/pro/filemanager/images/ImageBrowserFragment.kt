package pro.filemanager.images

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
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
import pro.filemanager.core.PermissionWrapper
import pro.filemanager.core.SimpleInjector
import pro.filemanager.core.UIManager
import pro.filemanager.core.base.BaseFragment
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.core.tools.ShareTool
import pro.filemanager.core.tools.sort.SortBottomModalSheetFragment
import pro.filemanager.core.tools.sort.SortTool
import pro.filemanager.databinding.FragmentImageBrowserBinding
import pro.filemanager.images.albums.ImageAlbumItem
import kotlin.math.log

class ImageBrowserFragment : BaseFragment(), Observer<MutableList<ImageItem>> {

    lateinit var binding: FragmentImageBrowserBinding
    lateinit var viewModel: ImageBrowserViewModel

    var albumItem: ImageAlbumItem? = null
    lateinit var onBackCallback: OnBackPressedCallback

    lateinit var searchView: SearchView

    override fun onChanged(t: MutableList<ImageItem>?) {
        if(binding.fragmentImageBrowserList.adapter != null && this::viewModel.isInitialized) {
            try {
                viewModel.MainScope?.cancel()
                viewModel.MainScope = CoroutineScope(Main)
            } catch(thr: Throwable) {

            }

            (binding.fragmentImageBrowserList.adapter as ImageBrowserAdapter).imageItems = t!!
            binding.fragmentImageBrowserList.adapter!!.notifyDataSetChanged()

            binding.fragmentImageBrowserList.scrollToPosition(0)

            viewModel.searchInProgress = false

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageBrowserBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(binding.root)

        activity = requireActivity() as HomeActivity

        albumItem = arguments?.getParcelable(ImageCore.KEY_ARGUMENT_ALBUM_PARCELABLE)

        setHasOptionsMenu(true)

        activity.setSupportActionBar(binding.fragmentImageBrowserToolbarInclude.layoutBaseToolbar)

        onBackCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                navController.popBackStack(R.id.homeFragment, false)
            }
        }

        activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackCallback)
        if(albumItem == null) onBackCallback.isEnabled = true

        launchCore()

        binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout.post {
            binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout.height.let {
                binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomBarGalleryTitle.textSize = (it / 8).toFloat()
                binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomBarGalleryTitle.text = resources.getText(R.string.title_gallery)

                binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarAlbumsTitle.textSize = (it / 8).toFloat()
                binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarAlbumsTitle.text = resources.getText(R.string.title_folders)
            }
        }

        if(albumItem != null) {
            binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarAlbumsTitle.setTypeface(null, Typeface.BOLD)
            binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarAlbumsTitleIndicator.visibility = View.VISIBLE
        } else {
            binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomBarGalleryTitle.setTypeface(null, Typeface.BOLD)
            binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomBarGalleryTitleIndicator.visibility = View.VISIBLE
        }

        binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarGalleryTitleContainer.setOnClickListener {
            if(albumItem != null) {
                navController.navigate(R.id.action_imageBrowserFragment_self)
            }
        }

        binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarAlbumsTitleContainer.setOnClickListener {
            navController.navigate(R.id.action_imageBrowserFragment_to_imageAlbumsFragment)
        }

        binding.fragmentImageBrowserBottomToolBarInclude.layoutBottomToolBarDeleteContainer.setOnClickListener {

            try {
//                if(this::viewModel.isInitialized && viewModel.selectionTool != null && viewModel.selectionTool!!.selectionMode && viewModel.selectionTool!!.selectedPositions.isNotEmpty()) {
//
//                    viewModel.selectionTool!!.selectedPositions.forEach {
//                        requireContext().contentResolver.delete(Uri.parse((binding.fragmentImageBrowserList.adapter as ImageBrowserAdapter).imageItems[it].data), null, null)
//                    }
//
//                    ApplicationLoader.ApplicationIOScope.launch {
//                        ImageRepo.getInstance().loadItems(requireContext(), true)
//                    }
//                }
            } catch (thr: Throwable) {

            }
        }
    }

    private fun initAdapter(imageItems: MutableList<ImageItem>) {
        binding.fragmentImageBrowserList.layoutManager = GridLayoutManager(context, UIManager.getItemGridSpanNumber(requireActivity()))

        binding.fragmentImageBrowserList.layoutManager?.onRestoreInstanceState(viewModel.mainListRvState)

        binding.fragmentImageBrowserList.adapter = ImageBrowserAdapter(requireActivity(), imageItems, layoutInflater, this@ImageBrowserFragment)

        binding.fragmentImageBrowserList.itemAnimator = object : DefaultItemAnimator() {

            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }

        binding.fragmentImageBrowserList.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dx > 0 || dy > 0) {
                    if(this@ImageBrowserFragment::viewModel.isInitialized && viewModel.selectionTool != null && !viewModel.selectionTool!!.selectionMode)
                        binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.GONE
                } else {
                    if(this@ImageBrowserFragment::viewModel.isInitialized && viewModel.selectionTool != null && !viewModel.selectionTool!!.selectionMode)
                        binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.VISIBLE
                }
            }
        })

    }

    fun launchCore() {
        activity.requestExternalStoragePermission {

            ApplicationLoader.ApplicationIOScope.launch {
                viewModel = ViewModelProviders.of(this@ImageBrowserFragment, SimpleInjector.provideImageBrowserViewModelFactory(albumItem)).get(ImageBrowserViewModel::class.java)

                withContext(Main) {
                    viewModel.getItemsLive(frContext).observe(viewLifecycleOwner, this@ImageBrowserFragment)

                    try {

                        if(viewModel.selectionTool == null) viewModel.selectionTool = SelectionTool()

                        initAdapter(viewModel.getItemsLive(frContext).value!!)

                        viewModel.selectionTool!!.initOnBackCallback (activity,
                                binding.fragmentImageBrowserList.adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>,
                                binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb,
                                binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                                binding.fragmentImageBrowserBottomToolBarInclude.layoutBottomToolBarRootLayout,
                                binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout)

                        binding.fragmentImageBrowserBottomToolBarInclude.layoutBottomToolBarRootLayout.post {
                            binding.fragmentImageBrowserBottomToolBarInclude.layoutBottomToolBarRootLayout.height.let {
                                binding.fragmentImageBrowserBottomToolBarInclude.layoutBottomToolBarShareTitle?.textSize = (it / 10).toFloat()
                                binding.fragmentImageBrowserBottomToolBarInclude.layoutBottomToolBarMoveTitle?.textSize = (it / 10).toFloat()
                                binding.fragmentImageBrowserBottomToolBarInclude.layoutBottomToolBarCopyTitle?.textSize = (it / 10).toFloat()
                                binding.fragmentImageBrowserBottomToolBarInclude.layoutBottomToolBarInfoTitle?.textSize = (it / 10).toFloat()
                                binding.fragmentImageBrowserBottomToolBarInclude.layoutBottomToolBarDeleteTitle?.textSize = (it / 10).toFloat()
                            }
                        }

                        if(viewModel.selectionTool!!.selectionMode) {
                            if (viewModel.selectionTool!!.selectedPositions.isNotEmpty()) {
                                activity.supportActionBar?.hide()
                                binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout.visibility = View.VISIBLE
                                binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb.text = viewModel.selectionTool!!.selectedPositions.size.toString()
                            }

                            binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.GONE
                            binding.fragmentImageBrowserBottomToolBarInclude.layoutBottomToolBarRootLayout.visibility = View.VISIBLE
                        }

                        binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
                            if (b) {
                                viewModel.selectionTool!!.selectAll(binding.fragmentImageBrowserList.adapter!!, binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb)
                            } else {
                                viewModel.selectionTool!!.unselectAll(binding.fragmentImageBrowserList.adapter!!, binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb)
                            }
                        }

                        binding.fragmentImageBrowserBottomToolBarInclude.layoutBottomToolBarShareContainer.setOnClickListener {
                            if(this@ImageBrowserFragment::viewModel.isInitialized &&
                                    viewModel.selectionTool != null &&
                                    !viewModel.selectionTool!!.selectedPositions.isNullOrEmpty()
                            ) {
                                try {
                                    val paths = mutableListOf<String>()

                                    viewModel.selectionTool!!.selectedPositions.forEach {
                                        paths.add((binding.fragmentImageBrowserList.adapter as ImageBrowserAdapter).imageItems[it].data)
                                    }

                                    ShareTool.shareFiles(frContext, paths)
                                } catch(thr: Throwable) {

                                }
                            }
                        }
                    } catch (thr: Throwable) {

                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if(albumItem != null) {
            activity.supportActionBar?.title = albumItem!!.displayName
        } else {
            activity.supportActionBar?.title = requireContext().resources.getString(R.string.title_images)
        }

        if(ApplicationLoader.isUserSentToAppDetailsSettings && PermissionWrapper.checkExternalStoragePermissions(frContext)) {
            launchCore()
            ApplicationLoader.isUserSentToAppDetailsSettings = false
        } else if(ApplicationLoader.isUserSentToAppDetailsSettings && !PermissionWrapper.checkExternalStoragePermissions(frContext)) {
            ApplicationLoader.isUserSentToAppDetailsSettings = false
            activity.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_toolbar_menu, menu)

        searchView = menu.findItem(R.id.mainToolbarMenuItemSearch).actionView as SearchView

        searchView.apply {
            imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI

            setOnSearchClickListener {
                if(this@ImageBrowserFragment::viewModel.isInitialized)
                    viewModel.isSearchViewEnabled = true
            }

            setOnCloseListener {
                if(this@ImageBrowserFragment::viewModel.isInitialized)
                    viewModel.isSearchViewEnabled = false
                false
            }

            if(this@ImageBrowserFragment::viewModel.isInitialized && viewModel.isSearchViewEnabled) {
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
                    if(this@ImageBrowserFragment::viewModel.isInitialized) {
                        viewModel.IOScope.launch {
                            viewModel.setSearchText(newText)

                            viewModel.assignItemsLive(frContext)
                        }
                    }

                    return true
                }
            })
        }

        menu.findItem(R.id.mainToolbarMenuItemSort).setOnMenuItemClickListener {

            if(this@ImageBrowserFragment::viewModel.isInitialized) {
                val sortBottomModalSheetFragment = SortBottomModalSheetFragment()
                sortBottomModalSheetFragment.arguments = bundleOf(SortTool.KEY_ARGUMENT_SORTING_VIEW_MODEL to viewModel)

                sortBottomModalSheetFragment.show(requireActivity().supportFragmentManager, null)
            }

            true
        }

        menu.findItem(R.id.mainToolbarMenuItemEdit).setOnMenuItemClickListener {

            if(this@ImageBrowserFragment::viewModel.isInitialized && viewModel.selectionTool != null && binding.fragmentImageBrowserList.adapter != null) {
                viewModel.selectionTool!!.enterMode(activity,
                        binding.fragmentImageBrowserList.adapter!!,
                        binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb,
                        binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                        binding.fragmentImageBrowserBottomToolBarInclude.layoutBottomToolBarRootLayout,
                        binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout
                )
            }

            true
        }
    }

    override fun onStop() {
        super.onStop()

        if(this::viewModel.isInitialized) viewModel.mainListRvState = binding.fragmentImageBrowserList.layoutManager?.onSaveInstanceState()
    }

}