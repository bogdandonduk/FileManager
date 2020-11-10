package pro.filemanager.files

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.*
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity
import pro.filemanager.R
import pro.filemanager.audios.AudioManager
import pro.filemanager.databinding.FragmentFileBrowserBinding
import pro.filemanager.docs.DocManager
import pro.filemanager.images.ImageManager
import pro.filemanager.videos.VideoManager
import java.io.File

class FileBrowserFragment() : Fragment() {

    lateinit var binding: FragmentFileBrowserBinding
    lateinit var navController: NavController

    lateinit var activity: HomeActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = requireActivity() as HomeActivity

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFileBrowserBinding.inflate(inflater, container, false)

        binding.fragmentFileBrowserList.layoutManager = LinearLayoutManager(context)

        (requireActivity() as HomeActivity).requestExternalStoragePermission {

            ApplicationLoader.ApplicationIOScope.launch {

                val path = requireArguments().getString("path", "")

                if(path == FileManager.internalRootPath) {
                    activity.supportActionBar?.title = activity.resources.getString(R.string.title_internal_storage)
                } else if(path == FileManager.externalRootPath) {
                    activity.supportActionBar?.title = activity.resources.getString(R.string.title_external_storage)
                }

                val files = File(path).listFiles()

                withContext(Dispatchers.Main) {
                    initAdapter(files!!)
                }

                if(ImageManager.preloadedImages == null && !ImageManager.preloadingInProgress){
                    ApplicationLoader.ApplicationIOScope.launch {
                        ImageManager.preloadImages(requireContext())
                    }
                } else if(VideoManager.preloadedVideos == null && !VideoManager.preloadingVideosInProgress) {
                    ApplicationLoader.ApplicationIOScope.launch {
                        VideoManager.preloadVideos(requireContext())
                    }
                } else if(DocManager.preloadedDocs == null && !DocManager.preloadingInProgress) {
                    ApplicationLoader.ApplicationIOScope.launch {
                        DocManager.preloadDocs(requireContext())
                    }
                } else if(AudioManager.preloadedAudios == null && !AudioManager.preloadingInProgress) {
                    ApplicationLoader.ApplicationIOScope.launch {
                        AudioManager.preloadAudios(requireContext())
                    }
                }
            }

        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(binding.root)

    }

    fun initAdapter(files: Array<File>) {

        binding.fragmentFileBrowserList.adapter = FileBrowserAdapter(requireActivity(), files, layoutInflater)

    }

    fun navigate(actionId: Int, bundle: Bundle) {
        navController.navigate(actionId, bundle)
    }
}