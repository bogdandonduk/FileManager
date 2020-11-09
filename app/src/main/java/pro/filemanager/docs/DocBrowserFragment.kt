package pro.filemanager.docs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.*
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity
import pro.filemanager.audios.AudioManager
import pro.filemanager.databinding.FragmentDocBrowserBinding
import pro.filemanager.files.FileManager
import pro.filemanager.images.ImageManager
import pro.filemanager.videos.VideoManager

class DocBrowserFragment() : Fragment() {

    lateinit var binding: FragmentDocBrowserBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDocBrowserBinding.inflate(inflater, container, false)

        binding.fragmentDocBrowserList.layoutManager = LinearLayoutManager(context)

        (requireActivity() as HomeActivity).requestExternalStoragePermission {

            ApplicationLoader.ApplicationIOScope.launch {
                val docItems: MutableList<DocItem> = if(DocManager.preloadedDocs != null) {

                    DocManager.preloadedDocs!!
                } else {
                    if(!DocManager.preloadingInProgress) {
                        DocManager.preloadDocs(requireContext())
                        DocManager.preloadedDocs!!
                    } else {
                        while(DocManager.preloadingInProgress && DocManager.preloadedDocs == null) {
                            delay(25)
                        }

                        DocManager.preloadedDocs!!
                    }
                }

                withContext(Dispatchers.Main) {
                    initAdapter(docItems)
                }

                if(ImageManager.preloadedImages == null && !ImageManager.preloadingInProgress){
                    ApplicationLoader.ApplicationIOScope.launch {
                        ImageManager.preloadImages(requireContext())
                    }
                } else if(VideoManager.preloadedVideos == null && !VideoManager.preloadingVideosInProgress) {
                    ApplicationLoader.ApplicationIOScope.launch {
                        VideoManager.preloadVideos(requireContext())
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

    fun initAdapter(docItems: MutableList<DocItem>) {

        binding.fragmentDocBrowserList.adapter = DocBrowserAdapter(requireActivity(), docItems, layoutInflater)

    }
}