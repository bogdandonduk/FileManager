package pro.filemanager.images

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.CompoundButton
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity
import pro.filemanager.R
import pro.filemanager.core.SimpleInjector
import pro.filemanager.core.UIManager
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.databinding.FragmentImageBrowserBinding
import pro.filemanager.images.albums.ImageAlbumItem
import java.lang.IllegalStateException

class ImageBrowserFragment : Fragment(), Observer<MutableList<ImageItem>> {

    lateinit var binding: FragmentImageBrowserBinding
    lateinit var activity: HomeActivity
    lateinit var navController: NavController
    lateinit var viewModel: ImageBrowserViewModel
    var albumItem: ImageAlbumItem? = null

    val IOScope = CoroutineScope(IO)
    val MainScope = CoroutineScope(Main)

    override fun onChanged(t: MutableList<ImageItem>?) {
        if(binding.fragmentImageBrowserList.adapter != null) {
            (binding.fragmentImageBrowserList.adapter as ImageBrowserAdapter).imageItems = t!!
            binding.fragmentImageBrowserList.adapter!!.notifyDataSetChanged()

            binding.fragmentImageBrowserList.scrollToPosition(0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = requireActivity() as HomeActivity

        albumItem = arguments?.getParcelable(ImageCore.KEY_ARGUMENT_ALBUM_PARCELABLE)
        
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImageBrowserBinding.inflate(inflater, container, false)

        activity.setSupportActionBar(binding.fragmentImageBrowserToolbarInclude.layoutBaseToolbar)

        activity.requestExternalStoragePermission {

            ApplicationLoader.ApplicationIOScope.launch {
                viewModel = ViewModelProviders.of(this@ImageBrowserFragment, SimpleInjector.provideImageBrowserViewModelFactory(albumItem)).get(ImageBrowserViewModel::class.java)

                withContext(Main) {

                    viewModel.getItemsLive().observe(viewLifecycleOwner, this@ImageBrowserFragment)

                    try {
                        initAdapter(viewModel.getItemsLive().value!!)
                    } catch(e: IllegalStateException) {
                        e.printStackTrace()

                        // TODO: MediaStore fetching failed with IllegalStateException.
                        //  Most likely, it is something out of our hands.
                        //  Show "Something went wrong" dialog

                    }
                }

                if(viewModel.selectionTool == null)
                    viewModel.selectionTool = SelectionTool()

                viewModel.selectionTool!!.initOnBackCallback(activity, binding.fragmentImageBrowserList.adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>, binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb)

                if(viewModel.selectionTool!!.selectionMode && viewModel.selectionTool!!.selectedPositions.isNotEmpty()) {
                    activity.supportActionBar?.hide()
                    binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout.visibility = View.VISIBLE
                    binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb.text = viewModel.selectionTool!!.selectedPositions.size.toString()
                }

                binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
                    if(b) {
                        viewModel.selectionTool!!.selectAll(binding.fragmentImageBrowserList.adapter!!, binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb)
                    } else {
                        viewModel.selectionTool!!.unselectAll(binding.fragmentImageBrowserList.adapter!!, binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb)
                    }
                }
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        if(albumItem != null) {
            activity.supportActionBar?.title = albumItem!!.displayName
        } else {
            activity.supportActionBar?.title = requireContext().resources.getString(R.string.title_images)
        }

        binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb.setButtonDrawable(R.drawable.bg_checkbox)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(binding.root)

        binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout.post {
            binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout.height.let {
                binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomBarGalleryTitle.textSize = (it / 8).toFloat()
                binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomBarGalleryTitle.text = resources.getText(R.string.title_image_gallery)

                binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarAlbumsTitle.textSize = (it / 8).toFloat()
                binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarAlbumsTitle.text = resources.getText(R.string.title_image_albums)
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
                    binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.GONE
                } else {
                    binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout.visibility = View.VISIBLE

                }
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.common_toolbar_menu, menu)

        val searchView = menu.findItem(R.id.imageBrowserToolbarMenuItemSearch).actionView as SearchView

        searchView.post {
            searchView.apply {
                if(this@ImageBrowserFragment::viewModel.isInitialized && !viewModel.currentSearchText.isNullOrEmpty()) {
                    setQuery(viewModel.currentSearchText, false)
                    isIconified = false
                    requestFocus()

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

        viewModel.mainListRvState = binding.fragmentImageBrowserList.layoutManager?.onSaveInstanceState()

    }

    override fun onDestroy() {
        super.onDestroy()

        IOScope.cancel()
        MainScope.cancel()

    }

}