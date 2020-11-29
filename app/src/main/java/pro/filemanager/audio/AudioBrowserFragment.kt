package pro.filemanager.audio

import android.graphics.Typeface
import android.os.Bundle
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
import pro.filemanager.audio.albums.AudioAlbumItem
import pro.filemanager.core.KEY_TRANSIENT_PARCELABLE_ALBUMS_MAIN_LIST_RV_STATE
import pro.filemanager.core.KEY_TRANSIENT_STRINGS_ALBUMS_SEARCH_TEXT
import pro.filemanager.core.SimpleInjector
import pro.filemanager.core.UIManager
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.databinding.FragmentAudioBrowserBinding

class AudioBrowserFragment : Fragment(), Observer<MutableList<AudioItem>> {

    lateinit var binding: FragmentAudioBrowserBinding
    lateinit var activity: HomeActivity
    lateinit var navController: NavController
    lateinit var viewModel: AudioBrowserViewModel

    var albumItem: AudioAlbumItem? = null
    lateinit var onBackCallback: OnBackPressedCallback

    lateinit var searchView: SearchView

    override fun onChanged(t: MutableList<AudioItem>?) {

        if(binding.fragmentAudioBrowserList.adapter != null) {
            try {
                viewModel.MainScope?.cancel()
                viewModel.MainScope = null
                viewModel.MainScope = CoroutineScope(Main)
            } catch(thr: Throwable) {

            }

            (binding.fragmentAudioBrowserList.adapter as AudioBrowserAdapter).audioItems = t!!
            binding.fragmentAudioBrowserList.adapter!!.notifyDataSetChanged()

            binding.fragmentAudioBrowserList.scrollToPosition(0)

            viewModel.searchInProgress = false

        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentAudioBrowserBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(binding.root)

        activity = requireActivity() as HomeActivity

        albumItem = arguments?.getParcelable(AudioCore.KEY_ARGUMENT_ALBUM_PARCELABLE)

        setHasOptionsMenu(true)

        activity.setSupportActionBar(binding.fragmentAudioBrowserToolbarInclude.layoutBaseToolbar)

        onBackCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                navController.popBackStack(R.id.homeFragment, false)
            }
        }

        activity.requestExternalStoragePermission {

            ApplicationLoader.ApplicationIOScope.launch {
                viewModel = ViewModelProviders.of(this@AudioBrowserFragment, SimpleInjector.provideAudioBrowserViewModelFactory(albumItem)).get(AudioBrowserViewModel::class.java)

                withContext(Main) {
                    viewModel.getItemsLive().observe(viewLifecycleOwner, this@AudioBrowserFragment)

                    try {
                        initAdapter(viewModel.getItemsLive().value!!)
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()

                        // TODO: MediaStore fetching failed with IllegalStateException.
                        //  Most likely, it is something out of our hands.
                        //  Show "Something went wrong" dialog

                    }

                    if (viewModel.selectionTool == null)
                        viewModel.selectionTool = SelectionTool()

                    viewModel.selectionTool!!.initOnBackCallback(
                            activity,
                            binding.fragmentAudioBrowserList.adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>,
                            binding.fragmentAudioBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb,
                            binding.fragmentAudioBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                            binding.fragmentAudioBrowserBottomToolBarInclude.layoutBottomToolBarRootLayout,
                            binding.fragmentAudioBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout)

                    if (viewModel.selectionTool!!.selectionMode) {
                        if (viewModel.selectionTool!!.selectedPositions.isNotEmpty()) {
                            activity.supportActionBar?.hide()
                            binding.fragmentAudioBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout.visibility = View.VISIBLE
                            binding.fragmentAudioBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb.text = viewModel.selectionTool!!.selectedPositions.size.toString()
                        }

                        binding.fragmentAudioBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.GONE
                        binding.fragmentAudioBrowserBottomToolBarInclude.layoutBottomToolBarRootLayout.visibility = View.VISIBLE
                    }

                    binding.fragmentAudioBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
                        if (b) {
                            viewModel.selectionTool!!.selectAll(binding.fragmentAudioBrowserList.adapter!!, binding.fragmentAudioBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb)
                        } else {
                            viewModel.selectionTool!!.unselectAll(binding.fragmentAudioBrowserList.adapter!!, binding.fragmentAudioBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb)
                        }
                    }
                }
            }
        }

        activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackCallback)
        if(albumItem == null) onBackCallback.isEnabled = true

        binding.fragmentAudioBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout.post {
            binding.fragmentAudioBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout.height.let {
                binding.fragmentAudioBrowserBottomTabsBarInclude.layoutBottomBarGalleryTitle.textSize = (it / 8).toFloat()
                binding.fragmentAudioBrowserBottomTabsBarInclude.layoutBottomBarGalleryTitle.text = resources.getText(R.string.title_library)

                binding.fragmentAudioBrowserBottomTabsBarInclude.layoutBottomTabsBarAlbumsTitle.textSize = (it / 8).toFloat()
                binding.fragmentAudioBrowserBottomTabsBarInclude.layoutBottomTabsBarAlbumsTitle.text = resources.getText(R.string.title_folders)
            }
        }

        if(albumItem != null) {
            binding.fragmentAudioBrowserBottomTabsBarInclude.layoutBottomTabsBarAlbumsTitle.setTypeface(null, Typeface.BOLD)
            binding.fragmentAudioBrowserBottomTabsBarInclude.layoutBottomTabsBarAlbumsTitleIndicator.visibility = View.VISIBLE
        } else {
            binding.fragmentAudioBrowserBottomTabsBarInclude.layoutBottomBarGalleryTitle.setTypeface(null, Typeface.BOLD)
            binding.fragmentAudioBrowserBottomTabsBarInclude.layoutBottomBarGalleryTitleIndicator.visibility = View.VISIBLE
        }

        binding.fragmentAudioBrowserBottomTabsBarInclude.layoutBottomTabsBarGalleryTitleContainer.setOnClickListener {
            if(albumItem != null) {
                navController.navigate(R.id.action_audioBrowserFragment_self)
            }
        }

        binding.fragmentAudioBrowserBottomTabsBarInclude.layoutBottomTabsBarAlbumsTitleContainer.setOnClickListener {
            navController.navigate(R.id.action_audioBrowserFragment_to_audioAlbumsFragment)
        }

        binding.fragmentAudioBrowserBottomToolBarInclude.layoutBottomToolBarDeleteContainer.setOnClickListener {
            try {
                if(this::viewModel.isInitialized && viewModel.selectionTool != null && viewModel.selectionTool!!.selectionMode && viewModel.selectionTool!!.selectedPositions.isNotEmpty()) {

                    viewModel.selectionTool!!.selectedPositions.forEach {

                    }

                    ApplicationLoader.ApplicationIOScope.launch {
                        AudioRepo.getInstance().loadItems(requireContext(), true)
                    }
                }
            } catch (thr: Throwable) {

            }
        }
    }

    private fun initAdapter(imageItems: MutableList<AudioItem>) {
        binding.fragmentAudioBrowserList.layoutManager = GridLayoutManager(context, UIManager.getItemGridSpanNumber(requireActivity()))
        binding.fragmentAudioBrowserList.layoutManager?.onRestoreInstanceState(viewModel.mainListRvState)

        binding.fragmentAudioBrowserList.adapter = AudioBrowserAdapter(requireActivity(), imageItems, layoutInflater, this@AudioBrowserFragment)

        binding.fragmentAudioBrowserList.itemAnimator = object : DefaultItemAnimator() {

            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }

        binding.fragmentAudioBrowserList.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dx > 0 || dy > 0) {
                    if(this@AudioBrowserFragment::viewModel.isInitialized && viewModel.selectionTool != null && !viewModel.selectionTool!!.selectionMode)
                        binding.fragmentAudioBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.GONE
                } else {
                    if(this@AudioBrowserFragment::viewModel.isInitialized && viewModel.selectionTool != null && !viewModel.selectionTool!!.selectionMode)
                        binding.fragmentAudioBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.VISIBLE

                }
            }
        })

    }

    override fun onResume() {
        super.onResume()

        if(albumItem != null) {
            activity.supportActionBar?.title = albumItem!!.displayName
        } else {
            activity.supportActionBar?.title = requireContext().resources.getString(R.string.title_audio)
        }

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
                    false
                }

                if(this@AudioBrowserFragment::viewModel.isInitialized && viewModel.isSearchViewEnabled) {
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

                        return false
                    }

                })
            }
        }
    }

    override fun onStop() {
        super.onStop()

        if(this::viewModel.isInitialized)
            viewModel.mainListRvState = binding.fragmentAudioBrowserList.layoutManager?.onSaveInstanceState()

    }

}