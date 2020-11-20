package pro.filemanager.images.albums

import android.os.Bundle
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
import pro.filemanager.core.SimpleInjector
import pro.filemanager.core.UIManager
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.databinding.FragmentImageAlbumsBinding
import java.lang.IllegalStateException

class ImageAlbumsFragment : Fragment(), Observer<MutableList<ImageAlbumItem>> {

    lateinit var binding: FragmentImageAlbumsBinding
    lateinit var activity: HomeActivity
    lateinit var viewModel: ImageAlbumsViewModel
    var mainAdapter: ImageAlbumsAdapter? = null

    val IOScope = CoroutineScope(Dispatchers.IO)
    val MainScope = CoroutineScope(Main)

    override fun onChanged(t: MutableList<ImageAlbumItem>?) {
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
        binding = FragmentImageAlbumsBinding.inflate(inflater, container, false)

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
                viewModel.selectionTool!!.overrideOnBackBehavior(requireActivity() as HomeActivity, mainAdapter as RecyclerView.Adapter<RecyclerView.ViewHolder>)

            }
        }

        return binding.root
    }

    private fun initAdapter(imageAlbumItems: MutableList<ImageAlbumItem>) {
        binding.fragmentImageAlbumsList.layoutManager = GridLayoutManager(context, UIManager.getAlbumGridSpanNumber(requireActivity()))
        (binding.fragmentImageAlbumsList.layoutManager as GridLayoutManager).onRestoreInstanceState(viewModel.mainRvScrollPosition)

        (binding.fragmentImageAlbumsList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        mainAdapter = ImageAlbumsAdapter(requireActivity(), imageAlbumItems, layoutInflater, this@ImageAlbumsFragment)

        binding.fragmentImageAlbumsList.adapter = mainAdapter

    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.mainRvScrollPosition = (binding.fragmentImageAlbumsList.layoutManager as GridLayoutManager).onSaveInstanceState()

        IOScope.cancel()
        MainScope.cancel()
    }

}