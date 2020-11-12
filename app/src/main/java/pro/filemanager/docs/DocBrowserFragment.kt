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
                val docItems: MutableList<DocItem> = if(DocManager.loadedDocs != null) {

                    DocManager.loadedDocs!!
                } else {
                    if(!DocManager.loadingInProgress) {
                        DocManager.loadDocs(requireContext())
                        DocManager.loadedDocs!!
                    } else {
                        while(DocManager.loadingInProgress && DocManager.loadedDocs == null) {
                            delay(25)
                        }

                        DocManager.loadedDocs!!
                    }
                }

                withContext(Dispatchers.Main) {
                    initAdapter(docItems)
                }

            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        if(VideoManager.loadedVideos == null && !VideoManager.loadingInProgress){
            ApplicationLoader.loadVideos()
        } else if(ImageManager.loadedImages == null && !ImageManager.loadingInProgress) {
            ApplicationLoader.loadImages()
        } else if(FileManager.externalRootPath == null && !FileManager.findingExternalRootInProgress) {
            ApplicationLoader.findExternalRoot()
        } else if(AudioManager.loadedAudios == null && !AudioManager.loadingInProgress) {
            ApplicationLoader.loadAudios()
        }
    }

    fun initAdapter(docItems: MutableList<DocItem>) {
        binding.fragmentDocBrowserList.adapter = DocBrowserAdapter(requireActivity(), docItems, layoutInflater)
    }
}