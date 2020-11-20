package pro.filemanager.images.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity
import pro.filemanager.core.SimpleInjector
import pro.filemanager.core.UIManager
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.databinding.FragmentImageGalleryBinding
import pro.filemanager.images.ImageCore
import pro.filemanager.images.ImageItem
import java.lang.IllegalStateException

class ImageGalleryFragment : Fragment(), Observer<MutableList<ImageItem>> {

    lateinit var binding: FragmentImageGalleryBinding
    lateinit var activity: HomeActivity
    lateinit var viewModel: ImageGalleryViewModel
    var mainAdapter: ImageGalleryAdapter? = null

    val IOScope = CoroutineScope(Dispatchers.IO)
    val MainScope = CoroutineScope(Main)

    override fun onChanged(t: MutableList<ImageItem>?) {
        mainAdapter?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = requireActivity() as HomeActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImageGalleryBinding.inflate(inflater, container, false)

        activity.requestExternalStoragePermission {

            ApplicationLoader.ApplicationIOScope.launch {

                viewModel = ViewModelProviders.of(this@ImageGalleryFragment, SimpleInjector.provideImageGalleryViewModelFactory(
                    arguments?.getParcelable(
                    ImageCore.KEY_ARGUMENT_ALBUM_PARCELABLE))).get(ImageGalleryViewModel::class.java)

                withContext(Main) {

                    viewModel.getItemsLive().observe(viewLifecycleOwner, this@ImageGalleryFragment)

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
                viewModel.selectionTool!!.overrideOnBackBehavior(requireActivity() as HomeActivity, mainAdapter as RecyclerView.Adapter<RecyclerView.ViewHolder>)

            }
        }

        return binding.root
    }

    private fun initAdapter(audioItems: MutableList<ImageItem>) {
        binding.fragmentImageGalleryList.layoutManager = GridLayoutManager(context, UIManager.getItemGridSpanNumber(requireActivity()))
        (binding.fragmentImageGalleryList.layoutManager as GridLayoutManager).onRestoreInstanceState(viewModel.mainRvScrollPosition)

        (binding.fragmentImageGalleryList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        mainAdapter = ImageGalleryAdapter(requireActivity(), audioItems, layoutInflater, this@ImageGalleryFragment)

        binding.fragmentImageGalleryList.adapter = mainAdapter

    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.mainRvScrollPosition = (binding.fragmentImageGalleryList.layoutManager as GridLayoutManager).onSaveInstanceState()

        IOScope.cancel()
        MainScope.cancel()
    }

}