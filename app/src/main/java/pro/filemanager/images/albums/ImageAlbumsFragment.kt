package pro.filemanager.images.albums

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity
import pro.filemanager.R
import pro.filemanager.core.KEY_TRANSIENT_PARCELABLE_IMAGE_ALBUMS_MAIN_LIST_RV_STATE
import pro.filemanager.core.SimpleInjector
import pro.filemanager.core.UIManager
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.databinding.FragmentImageAlbumsBinding
import java.lang.IllegalStateException

class ImageAlbumsFragment : Fragment(), Observer<MutableList<ImageAlbumItem>> {

    lateinit var binding: FragmentImageAlbumsBinding
    lateinit var navController: NavController
    lateinit var activity: HomeActivity
    lateinit var viewModel: ImageAlbumsViewModel

    val IOScope = CoroutineScope(IO)
    val MainScope = CoroutineScope(Main)

    lateinit var onBackCallback: OnBackPressedCallback

    override fun onChanged(t: MutableList<ImageAlbumItem>?) {
        binding.fragmentImageAlbumsList.adapter?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = requireActivity() as HomeActivity

        onBackCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navController.popBackStack(R.id.imageBrowserFragment, true)
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImageAlbumsBinding.inflate(inflater, container, false)

        activity.setSupportActionBar(binding.fragmentImageAlbumsToolbarInclude.layoutBaseToolbar)

        activity.requestExternalStoragePermission {
            ApplicationLoader.ApplicationIOScope.launch {
                viewModel = ViewModelProviders.of(this@ImageAlbumsFragment, SimpleInjector.provideImageAlbumsViewModelFactory()).get(ImageAlbumsViewModel::class.java)

                withContext(Main) {
                    try {
                        viewModel.getAlbumsLive().observe(viewLifecycleOwner, this@ImageAlbumsFragment)

                        initAdapter(viewModel.getAlbumsLive().value!!)

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
                viewModel.selectionTool!!.initOnBackCallback(activity, binding.fragmentImageAlbumsList.adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>)
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        activity.supportActionBar?.title = requireContext().resources.getString(R.string.title_image_albums)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(binding.root)

        binding.fragmentImageAlbumsBottomBarInclude.layoutBottomBarRootLayout.post {
            binding.fragmentImageAlbumsBottomBarInclude.layoutBottomBarRootLayout.height.let {
                binding.fragmentImageAlbumsBottomBarInclude.layoutBottomBarGalleryTitle.textSize = (it / 8).toFloat()
                binding.fragmentImageAlbumsBottomBarInclude.layoutBottomBarGalleryTitle.text = resources.getText(R.string.title_image_gallery)

                binding.fragmentImageAlbumsBottomBarInclude.layoutBottomBarAlbumsTitle.textSize = (it / 8).toFloat()
                binding.fragmentImageAlbumsBottomBarInclude.layoutBottomBarAlbumsTitle.text = resources.getText(R.string.title_image_albums)
            }
        }

        binding.fragmentImageAlbumsBottomBarInclude.layoutBottomBarAlbumsTitle.setTypeface(null, Typeface.BOLD)
        binding.fragmentImageAlbumsBottomBarInclude.layoutBottomBarAlbumsTitleIndicator.visibility = View.VISIBLE

        activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackCallback)

        binding.fragmentImageAlbumsBottomBarInclude.layoutBottomBarGalleryTitleContainer.setOnClickListener {
            onBackCallback.isEnabled = false
            ApplicationLoader.transientParcelables[KEY_TRANSIENT_PARCELABLE_IMAGE_ALBUMS_MAIN_LIST_RV_STATE] = binding.fragmentImageAlbumsList.layoutManager?.onSaveInstanceState()
            activity.onBackPressed()
        }

    }

    private fun initAdapter(imageAlbumItems: MutableList<ImageAlbumItem>) {
        binding.fragmentImageAlbumsList.layoutManager = GridLayoutManager(context, UIManager.getAlbumGridSpanNumber(requireActivity()))
        ApplicationLoader.transientParcelables[KEY_TRANSIENT_PARCELABLE_IMAGE_ALBUMS_MAIN_LIST_RV_STATE].let {
            if(it != null) {
                binding.fragmentImageAlbumsList.layoutManager?.onRestoreInstanceState(it)
                ApplicationLoader.transientParcelables.remove(KEY_TRANSIENT_PARCELABLE_IMAGE_ALBUMS_MAIN_LIST_RV_STATE)
            } else
                binding.fragmentImageAlbumsList.layoutManager?.onRestoreInstanceState(viewModel.mainListRvState)
        }

        binding.fragmentImageAlbumsList.adapter = ImageAlbumsAdapter(requireActivity(), imageAlbumItems, layoutInflater, this@ImageAlbumsFragment)
        binding.fragmentImageAlbumsList.adapter?.setHasStableIds(true)

        binding.fragmentImageAlbumsList.itemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }

        binding.fragmentImageAlbumsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dx > 0 || dy > 0) {
                    binding.fragmentImageAlbumsBottomBarInclude.layoutBottomBarRootLayout.visibility = View.GONE
                } else {
                    binding.fragmentImageAlbumsBottomBarInclude.layoutBottomBarRootLayout.visibility = View.VISIBLE

                }
            }
        })
    }

    override fun onStop() {
        super.onStop()

        viewModel.mainListRvState = binding.fragmentImageAlbumsList.layoutManager?.onSaveInstanceState()

    }

    override fun onDestroy() {
        super.onDestroy()

        IOScope.cancel()
        MainScope.cancel()
    }

}