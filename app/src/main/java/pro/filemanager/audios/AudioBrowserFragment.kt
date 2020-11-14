package pro.filemanager.audios

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity
import pro.filemanager.databinding.FragmentAudioBrowserBinding
import java.lang.IllegalStateException

class AudioBrowserFragment : Fragment(), Observer<MutableList<AudioItem>> {

    lateinit var binding: FragmentAudioBrowserBinding
    lateinit var viewModel: AudioBrowserViewModel
    var mainAdapter: AudioBrowserAdapter? = null
    var itemsLive: LiveData<MutableList<AudioItem>>? = null

    override fun onChanged(t: MutableList<AudioItem>?) {
        ApplicationLoader.ApplicationIOScope.launch {
            itemsLive = viewModel.getItemsLive()

            withContext(Main) {
                mainAdapter?.notifyDataSetChanged()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAudioBrowserBinding.inflate(inflater, container, false)

        binding.fragmentAudioBrowserList.layoutManager = LinearLayoutManager(context)

        (requireActivity() as HomeActivity).requestExternalStoragePermission {

            ApplicationLoader.ApplicationIOScope.launch {
                viewModel = ViewModelProviders.of(this@AudioBrowserFragment, AudioSimpleManualInjector.provideViewModelFactory()).get(AudioBrowserViewModel::class.java)

                withContext(Main) {

                    viewModel.getItemsLive().observe(viewLifecycleOwner, this@AudioBrowserFragment)

                    try {
                        initAdapter()
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
                ApplicationLoader.loadDocs()

            }

        }

        return binding.root
    }

    private fun initAdapter() {
        mainAdapter = AudioBrowserAdapter(requireActivity(), layoutInflater, this)
        binding.fragmentAudioBrowserList.adapter = mainAdapter

    }

}