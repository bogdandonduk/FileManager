package pro.filemanager.docs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity
import pro.filemanager.databinding.FragmentDocBrowserBinding
import java.lang.IllegalStateException

class DocBrowserFragment : Fragment(), Observer<MutableList<DocItem>> {

    lateinit var binding: FragmentDocBrowserBinding
    lateinit var viewModel: DocBrowserViewModel
    var mainAdapter: DocBrowserAdapter? = null

    override fun onChanged(t: MutableList<DocItem>?) {
        mainAdapter?.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDocBrowserBinding.inflate(inflater, container, false)

        binding.fragmentDocBrowserList.layoutManager = LinearLayoutManager(context)

        (requireActivity() as HomeActivity).requestExternalStoragePermission {

            ApplicationLoader.ApplicationIOScope.launch {
                viewModel = ViewModelProviders.of(this@DocBrowserFragment, DocSimpleManualInjector.provideViewModelFactory()).get(DocBrowserViewModel::class.java)

                withContext(Main) {

                    viewModel.getItemsLive().observe(viewLifecycleOwner, this@DocBrowserFragment)

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

                ApplicationLoader.loadVideos()
                ApplicationLoader.loadImages()
                ApplicationLoader.loadAudios()
            }

        }

        return binding.root
    }

    private fun initAdapter(audioItems: MutableList<DocItem>) {
        mainAdapter = DocBrowserAdapter(requireActivity(), audioItems, layoutInflater)
        binding.fragmentDocBrowserList.adapter = mainAdapter

    }

}