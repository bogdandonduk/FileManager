package pro.filemanager.images

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
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
import pro.filemanager.core.UIManager
import pro.filemanager.core.tools.SelectorTool
import pro.filemanager.databinding.FragmentImageBrowserBinding
import java.lang.IllegalStateException
import java.lang.Runnable

class ImageBrowserFragment : Fragment(), Observer<MutableList<ImageItem>> {

    lateinit var binding: FragmentImageBrowserBinding
    lateinit var viewModel: ImageBrowserViewModel
    var mainAdapter: ImageBrowserAdapter? = null

    val IOScope = CoroutineScope(Dispatchers.IO)
    val MainScope = CoroutineScope(Main)

    override fun onChanged(t: MutableList<ImageItem>?) {
        mainAdapter?.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImageBrowserBinding.inflate(inflater, container, false)

        (requireActivity() as HomeActivity).requestExternalStoragePermission {

            ApplicationLoader.ApplicationIOScope.launch {
                viewModel = ViewModelProviders.of(this@ImageBrowserFragment, ImageSimpleManualInjector.provideViewModelFactory()).get(ImageBrowserViewModel::class.java)

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

                    if(viewModel.selectorTool == null) {
                        viewModel.selectorTool = SelectorTool()
                    }

                    @Suppress("UNCHECKED_CAST")
                    viewModel.selectorTool!!.assignOnBackBehavior(requireActivity() as HomeActivity, mainAdapter as RecyclerView.Adapter<RecyclerView.ViewHolder>)
                }

                ApplicationLoader.loadVideos()
                ApplicationLoader.loadDocs()
                ApplicationLoader.loadAudios()

            }
        }

        return binding.root
    }

    private fun initAdapter(audioItems: MutableList<ImageItem>) {
        binding.fragmentImageBrowserList.layoutManager = GridLayoutManager(context, UIManager.getGridSpanNumber(requireActivity()))
        (binding.fragmentImageBrowserList.layoutManager as GridLayoutManager).onRestoreInstanceState(viewModel.mainRvScrollPosition)

        (binding.fragmentImageBrowserList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        mainAdapter = ImageBrowserAdapter(requireActivity(), audioItems, layoutInflater, this@ImageBrowserFragment)

        binding.fragmentImageBrowserList.adapter = mainAdapter

    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.mainRvScrollPosition = (binding.fragmentImageBrowserList.layoutManager as GridLayoutManager).onSaveInstanceState()

        IOScope.cancel()
        MainScope.cancel()
    }

}