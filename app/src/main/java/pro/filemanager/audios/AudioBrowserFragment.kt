package pro.filemanager.audios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity
import pro.filemanager.databinding.FragmentAudioBrowserBinding
import pro.filemanager.docs.DocManager
import pro.filemanager.files.FileManager
import pro.filemanager.images.ImageManager
import pro.filemanager.videos.VideoManager

class AudioBrowserFragment() : Fragment() {

    lateinit var binding: FragmentAudioBrowserBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAudioBrowserBinding.inflate(inflater, container, false)

        binding.fragmentAudioBrowserList.layoutManager = LinearLayoutManager(context)

        (requireActivity() as HomeActivity).requestExternalStoragePermission {

            ApplicationLoader.ApplicationIOScope.launch {
                val audioItems: MutableList<AudioItem> = if(AudioManager.loadedAudios != null) {

                    AudioManager.loadedAudios!!

                } else {
                    if(!AudioManager.loadingInProgress) {
                        AudioManager.loadAudios(requireContext())
                        AudioManager.loadedAudios!!
                    } else {
                        while(AudioManager.loadingInProgress && AudioManager.loadedAudios == null) {
                            delay(25)
                        }

                        AudioManager.loadedAudios!!
                    }

                }

                withContext(Main) {
                    initAdapter(audioItems)
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
        } else if(DocManager.loadedDocs == null && !DocManager.loadingInProgress) {
            ApplicationLoader.loadDocs()
        }
    }

    fun initAdapter(audioItems: MutableList<AudioItem>) {

        binding.fragmentAudioBrowserList.adapter = AudioBrowserAdapter(requireActivity(), audioItems, layoutInflater)

    }

}