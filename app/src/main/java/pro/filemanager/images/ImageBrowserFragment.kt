package pro.filemanager.images

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.coroutines.*
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
    var mainAdapter: ImageBrowserAdapter? = null
    var albumItem: ImageAlbumItem? = null

    val IOScope = CoroutineScope(Dispatchers.IO)
    val MainScope = CoroutineScope(Main)

    override fun onChanged(t: MutableList<ImageItem>?) {
        mainAdapter?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = requireActivity() as HomeActivity

        albumItem = arguments?.getParcelable(ImageCore.KEY_ARGUMENT_ALBUM_PARCELABLE)

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

                    } finally {


                    }

                }

                if(viewModel.selectionTool == null)
                    viewModel.selectionTool = SelectionTool()

                @Suppress("UNCHECKED_CAST")
                viewModel.selectionTool!!.initOnBackCallback(requireActivity() as HomeActivity, mainAdapter as RecyclerView.Adapter<RecyclerView.ViewHolder>)
                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, viewModel.selectionTool!!.onBackCallback!!)
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

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(binding.root)

        binding.fragmentImageBrowserBottomBarInclude.layoutBottomBarRootLayout.post {
            binding.fragmentImageBrowserBottomBarInclude.layoutBottomBarRootLayout.height.let {
                binding.fragmentImageBrowserBottomBarInclude.layoutBottomBarGalleryTitle.textSize = (it / 8).toFloat()
                binding.fragmentImageBrowserBottomBarInclude.layoutBottomBarGalleryTitle.text = resources.getText(R.string.title_image_gallery)

                binding.fragmentImageBrowserBottomBarInclude.layoutBottomBarAlbumsTitle.textSize = (it / 8).toFloat()
                binding.fragmentImageBrowserBottomBarInclude.layoutBottomBarAlbumsTitle.text = resources.getText(R.string.title_image_albums)
            }
        }

        if(albumItem != null) {
            binding.fragmentImageBrowserBottomBarInclude.layoutBottomBarAlbumsTitle.setTypeface(null, Typeface.BOLD)
            binding.fragmentImageBrowserBottomBarInclude.layoutBottomBarAlbumsTitleIndicator.visibility = View.VISIBLE
        } else {
            binding.fragmentImageBrowserBottomBarInclude.layoutBottomBarGalleryTitle.setTypeface(null, Typeface.BOLD)
            binding.fragmentImageBrowserBottomBarInclude.layoutBottomBarGalleryTitleIndicator.visibility = View.VISIBLE
        }

        binding.fragmentImageBrowserBottomBarInclude.layoutBottomBarGalleryTitleContainer.setOnClickListener {
            if(albumItem != null) {
                navController.navigate(R.id.action_imageBrowserFragment_self)
            }
        }

        binding.fragmentImageBrowserBottomBarInclude.layoutBottomBarAlbumsTitleContainer.setOnClickListener {
            navController.navigate(R.id.action_imageBrowserFragment_to_imageAlbumsFragment)
        }

    }

    private fun initAdapter(audioItems: MutableList<ImageItem>) {
        binding.fragmentImageBrowserList.layoutManager = GridLayoutManager(context, UIManager.getItemGridSpanNumber(requireActivity()))
        binding.fragmentImageBrowserList.layoutManager?.onRestoreInstanceState(viewModel.mainRvScrollPosition)

        (binding.fragmentImageBrowserList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        mainAdapter = ImageBrowserAdapter(requireActivity(), audioItems, layoutInflater, this@ImageBrowserFragment)

        binding.fragmentImageBrowserList.adapter = mainAdapter

        binding.fragmentImageBrowserList.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dx > 0 || dy > 0) {
                    binding.fragmentImageBrowserBottomBarInclude.layoutBottomBarRootLayout.visibility = View.GONE
                } else {
                    binding.fragmentImageBrowserBottomBarInclude.layoutBottomBarRootLayout.visibility = View.VISIBLE

                }
            }
        })

    }

    override fun onStop() {
        super.onStop()

        viewModel.mainRvScrollPosition = binding.fragmentImageBrowserList.layoutManager?.onSaveInstanceState()

    }

    override fun onDestroy() {
        super.onDestroy()

        IOScope.cancel()
        MainScope.cancel()

    }

}