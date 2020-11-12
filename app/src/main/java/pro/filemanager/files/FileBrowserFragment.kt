package pro.filemanager.files

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
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
import java.lang.Exception

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

        activity.requestExternalStoragePermission {

            ApplicationLoader.ApplicationIOScope.launch {

                withContext(Dispatchers.Main) {
                    activity.supportActionBar?.title = requireArguments().getString(FileManager.KEY_ARGUMENT_APP_BAR_TITLE)

                    try {
                        initAdapter(File(requireArguments().getString(FileManager.KEY_ARGUMENT_PATH)!!).listFiles()!!)
                    } catch (e: Exception) {

                    }

                }

                if(VideoManager.loadedVideos == null && !VideoManager.loadingInProgress){
                    ApplicationLoader.loadVideos()
                } else if(ImageManager.loadedImages == null && !ImageManager.loadingInProgress) {
                    ApplicationLoader.loadImages()
                } else if(FileManager.externalRootPath == null && !FileManager.findingExternalRootInProgress) {
                    ApplicationLoader.findExternalRoot()
                } else if(DocManager.loadedDocs == null && !DocManager.loadingInProgress) {
                    ApplicationLoader.loadDocs()
                } else if(AudioManager.loadedAudios == null && !AudioManager.loadingInProgress) {
                    ApplicationLoader.loadAudios()
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

        binding.fragmentFileBrowserList.adapter = FileBrowserAdapter(requireActivity(), files, layoutInflater, this@FileBrowserFragment)

    }

    fun navigate(path: String, appBarTitle: String = activity.supportActionBar?.title.toString()) {
        navController.navigate(R.id.action_fileBrowserFragment_self, bundleOf(
                FileManager.KEY_ARGUMENT_PATH to path,
                FileManager.KEY_ARGUMENT_APP_BAR_TITLE to appBarTitle
        ))
    }
}