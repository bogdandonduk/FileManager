package pro.filemanager.images.albums

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
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
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.databinding.FragmentImageAlbumsBinding
import java.lang.IllegalStateException

class ImageAlbumsFragment : Fragment(), Observer<MutableList<ImageAlbumItem>> {

    lateinit var binding: FragmentImageAlbumsBinding
    lateinit var navController: NavController
    lateinit var activity: HomeActivity
    lateinit var viewModel: ImageAlbumsViewModel

    lateinit var onBackCallback: OnBackPressedCallback
    lateinit var searchView: SearchView

    override fun onChanged(t: MutableList<ImageAlbumItem>?) {
        if(binding.fragmentImageAlbumsList.adapter != null) {
            try {
                viewModel.MainScope?.cancel()
                viewModel.MainScope = null
                viewModel.MainScope = CoroutineScope(Main)
            } catch(thr: Throwable) {

            }

            (binding.fragmentImageAlbumsList.adapter as ImageAlbumsAdapter).audioAlbumItems = t!!
            binding.fragmentImageAlbumsList.adapter!!.notifyDataSetChanged()

            binding.fragmentImageAlbumsList.scrollToPosition(0)

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

        activity.setSupportActionBar(binding.fragmentImageAlbumsToolbarInclude.layoutBaseToolbar)

        onBackCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navController.popBackStack(R.id.imageBrowserFragment, true)
            }
        }

        activity.requestExternalStoragePermission {
            ApplicationLoader.ApplicationIOScope.launch {
                viewModel = ViewModelProviders.of(this@ImageAlbumsFragment, SimpleInjector.provideImageAlbumsViewModelFactory()).get(ImageAlbumsViewModel::class.java)

                withContext(Main) {
                    try {
                        viewModel.getAlbumsLive().observe(viewLifecycleOwner, this@ImageAlbumsFragment)

                        ApplicationLoader.transientStrings[KEY_TRANSIENT_STRINGS_ALBUMS_SEARCH_TEXT].let {
                            if(!it.isNullOrEmpty()) {
                                viewModel.isSearchViewEnabled = true
                                viewModel.currentSearchText = it

                                viewModel.search(requireContext(), viewModel.currentSearchText)
                            }
                        }

                        initAdapter(viewModel.getAlbumsLive().value!!)

                    } catch (e: IllegalStateException) {
                        e.printStackTrace()

                        // TODO: MediaStore fetching failed with IllegalStateException.
                        //  Most likely, it is something out of our hands.
                        //  Show "Something went wrong" dialog

                    } finally {

                    }

                    if (viewModel.selectionTool == null)
                        viewModel.selectionTool = SelectionTool()

                    viewModel.selectionTool!!.initOnBackCallback(
                            activity,
                            binding.fragmentImageAlbumsList.adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>,
                            binding.fragmentImageAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb,
                            binding.fragmentImageAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                            binding.fragmentImageAlbumsBottomToolBarInclude.layoutBottomToolBarRootLayout,
                            binding.fragmentImageAlbumsBottomTabsBarInclude.layoutBottomTabsBarRootLayout
                    )

                    if (viewModel.selectionTool!!.selectionMode) {
                        if (viewModel.selectionTool!!.selectedPositions.isNotEmpty()) {
                            activity.supportActionBar?.hide()
                            binding.fragmentImageAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout.visibility = View.VISIBLE
                            binding.fragmentImageAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb.text = viewModel.selectionTool!!.selectedPositions.size.toString()
                        }

                        binding.fragmentImageAlbumsBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.GONE
                        binding.fragmentImageAlbumsBottomToolBarInclude.layoutBottomToolBarRootLayout.visibility = View.VISIBLE
                    }

                    binding.fragmentImageAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
                        if (b) {
                            viewModel.selectionTool!!.selectAll(binding.fragmentImageAlbumsList.adapter!!, binding.fragmentImageAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb)
                        } else {
                            viewModel.selectionTool!!.unselectAll(binding.fragmentImageAlbumsList.adapter!!, binding.fragmentImageAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb)
                        }
                    }
                }
            }
        }

        activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackCallback)

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
            ApplicationLoader.transientParcelables[KEY_TRANSIENT_PARCELABLE_ALBUMS_MAIN_LIST_RV_STATE] = binding.fragmentImageAlbumsList.layoutManager?.onSaveInstanceState()
            activity.onBackPressed()
        }

    }

    private fun initAdapter(audioAlbumItems: MutableList<ImageAlbumItem>) {
        binding.fragmentImageAlbumsList.layoutManager = GridLayoutManager(context, UIManager.getAlbumGridSpanNumber(requireActivity()))
        ApplicationLoader.transientParcelables[KEY_TRANSIENT_PARCELABLE_ALBUMS_MAIN_LIST_RV_STATE].let {
            if(it != null) {
                binding.fragmentImageAlbumsList.layoutManager?.onRestoreInstanceState(it)
                ApplicationLoader.transientParcelables.remove(KEY_TRANSIENT_PARCELABLE_ALBUMS_MAIN_LIST_RV_STATE)
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

    override fun onResume() {
        super.onResume()

        activity.supportActionBar?.title = requireContext().resources.getString(R.string.title_folders)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_toolbar_menu, menu)

        searchView = menu.findItem(R.id.mainToolbarMenuItemSearch).actionView as SearchView

        searchView.post {
            searchView.apply {
                imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI

                setOnSearchClickListener {
                    viewModel.isSearchViewEnabled = true
                }

                setOnCloseListener {
                    viewModel.isSearchViewEnabled = false
                    ApplicationLoader.transientStrings.remove(KEY_TRANSIENT_STRINGS_ALBUMS_SEARCH_TEXT)
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
                        viewModel.search(requireContext(), newText)
                        ApplicationLoader.transientStrings[KEY_TRANSIENT_STRINGS_ALBUMS_SEARCH_TEXT] = newText
                        return false
                    }

                })
            }
        }
    }

    override fun onStop() {
        super.onStop()

        if(this::viewModel.isInitialized)
            viewModel.mainListRvState = binding.fragmentImageAlbumsList.layoutManager?.onSaveInstanceState()

    }

}