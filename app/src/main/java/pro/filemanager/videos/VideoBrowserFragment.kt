package pro.filemanager.videos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity
import pro.filemanager.core.UIManager
import pro.filemanager.databinding.FragmentImageBrowserBinding
import pro.filemanager.databinding.FragmentVideoBrowserBinding
import java.lang.IllegalStateException

class VideoBrowserFragment : Fragment(), Observer<MutableList<VideoItem>> {

    lateinit var binding: FragmentVideoBrowserBinding
    lateinit var viewModel: VideoBrowserViewModel
    var mainAdapter: VideoBrowserAdapter? = null

    val IOScope = CoroutineScope(Dispatchers.IO)
    val MainScope = CoroutineScope(Main)

    override fun onChanged(t: MutableList<VideoItem>?) {
        mainAdapter?.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVideoBrowserBinding.inflate(inflater, container, false)

        (requireActivity() as HomeActivity).requestExternalStoragePermission {

            ApplicationLoader.ApplicationIOScope.launch {
                viewModel = ViewModelProviders.of(this@VideoBrowserFragment, VideoSimpleManualInjector.provideViewModelFactory()).get(VideoBrowserViewModel::class.java)

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

                ApplicationLoader.loadImages()
                ApplicationLoader.loadDocs()
                ApplicationLoader.loadAudios()

            }

        }

        return binding.root
    }

    private fun initAdapter(audioItems: MutableList<VideoItem>) {
        binding.fragmentVideoBrowserList.layoutManager = GridLayoutManager(context, UIManager.getGridSpanNumber(requireActivity()))
        (binding.fragmentVideoBrowserList.layoutManager as GridLayoutManager).onRestoreInstanceState(viewModel.mainRvScrollPosition)

        mainAdapter = VideoBrowserAdapter(requireActivity(), audioItems, layoutInflater, this@VideoBrowserFragment)

        binding.fragmentVideoBrowserList.adapter = mainAdapter

    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.mainRvScrollPosition = (binding.fragmentVideoBrowserList.layoutManager as GridLayoutManager).onSaveInstanceState()

        IOScope.cancel()
        MainScope.cancel()
    }
}