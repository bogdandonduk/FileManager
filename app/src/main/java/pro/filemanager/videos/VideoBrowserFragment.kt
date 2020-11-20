package pro.filemanager.videos

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
import pro.filemanager.R
import pro.filemanager.core.SimpleInjector
import pro.filemanager.core.UIManager
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.databinding.FragmentVideoBrowserBinding
import java.lang.IllegalStateException

class VideoBrowserFragment : Fragment(), Observer<MutableList<VideoItem>> {

    lateinit var binding: FragmentVideoBrowserBinding
    lateinit var activity: HomeActivity
    lateinit var viewModel: VideoBrowserViewModel
    var mainAdapter: VideoBrowserAdapter? = null

    val IOScope = CoroutineScope(Dispatchers.IO)
    val MainScope = CoroutineScope(Main)

    override fun onChanged(t: MutableList<VideoItem>?) {
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
        binding = FragmentVideoBrowserBinding.inflate(inflater, container, false)

        activity.requestExternalStoragePermission {

            ApplicationLoader.ApplicationIOScope.launch {
                viewModel = ViewModelProviders.of(this@VideoBrowserFragment, SimpleInjector.provideVideoBrowserViewModelFactory()).get(VideoBrowserViewModel::class.java)

                withContext(Main) {

                    viewModel.getItemsLive().observe(viewLifecycleOwner, this@VideoBrowserFragment)

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

                ApplicationLoader.loadImages()
                ApplicationLoader.findExternalRoots()
                ApplicationLoader.loadDocs()
                ApplicationLoader.loadAudios()
            }

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity.setSupportActionBar(binding.fragmentVideoBrowserLayoutBaseToolbarInclude.layoutBaseToolbar)
        activity.supportActionBar?.title = requireContext().resources.getString(R.string.title_videos)
    }

    private fun initAdapter(videoItems: MutableList<VideoItem>) {
        binding.fragmentVideoBrowserList.layoutManager = GridLayoutManager(context, UIManager.getItemGridSpanNumber(requireActivity()))
        (binding.fragmentVideoBrowserList.layoutManager as GridLayoutManager).onRestoreInstanceState(viewModel.mainRvScrollPosition)

        (binding.fragmentVideoBrowserList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        mainAdapter = VideoBrowserAdapter(requireActivity(), videoItems, layoutInflater, this@VideoBrowserFragment)

        binding.fragmentVideoBrowserList.adapter = mainAdapter

    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.mainRvScrollPosition = (binding.fragmentVideoBrowserList.layoutManager as GridLayoutManager).onSaveInstanceState()

        IOScope.cancel()
        MainScope.cancel()
    }
}