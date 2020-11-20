package pro.filemanager.audios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity
import pro.filemanager.R
import pro.filemanager.core.SimpleInjector
import pro.filemanager.databinding.FragmentAudioBrowserBinding
import java.lang.IllegalStateException

class AudioBrowserFragment : Fragment(), Observer<MutableList<AudioItem>> {

    lateinit var binding: FragmentAudioBrowserBinding
    lateinit var activity: HomeActivity
    lateinit var viewModel: AudioBrowserViewModel
    var mainAdapter: AudioBrowserAdapter? = null

    override fun onChanged(t: MutableList<AudioItem>?) {
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
        binding = FragmentAudioBrowserBinding.inflate(inflater, container, false)

        binding.fragmentAudioBrowserList.layoutManager = LinearLayoutManager(context)

       activity.requestExternalStoragePermission {

            ApplicationLoader.ApplicationIOScope.launch {
                viewModel = ViewModelProviders.of(this@AudioBrowserFragment, SimpleInjector.provideAudioBrowserViewModelFactory()).get(AudioBrowserViewModel::class.java)

                withContext(Main) {

                    viewModel.getItemsLive().observe(viewLifecycleOwner, this@AudioBrowserFragment)

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
                ApplicationLoader.findExternalRoots()
                ApplicationLoader.loadDocs()
            }

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity.setSupportActionBar(binding.fragmentAudioBrowserLayoutBaseToolbarInclude.layoutBaseToolbar)
        activity.supportActionBar?.title = requireContext().resources.getString(R.string.title_audios)
    }

    private fun initAdapter(audioItems: MutableList<AudioItem>) {
        mainAdapter = AudioBrowserAdapter(requireActivity(), audioItems, layoutInflater)
        binding.fragmentAudioBrowserList.adapter = mainAdapter

    }

}