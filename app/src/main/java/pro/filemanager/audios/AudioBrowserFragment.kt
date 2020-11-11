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
                val audioItems: MutableList<AudioItem> = if(AudioManager.preloadedAudios != null) {

                    AudioManager.preloadedAudios!!

                } else {
                    if(!AudioManager.preloadingInProgress) {
                        AudioManager.loadAudios(requireContext())
                        AudioManager.preloadedAudios!!
                    } else {
                        while(AudioManager.preloadingInProgress && AudioManager.preloadedAudios == null) {
                            delay(25)
                        }

                        AudioManager.preloadedAudios!!
                    }

                }

                withContext(Main) {
                    initAdapter(audioItems)
                }

                if(ImageManager.preloadedImages == null && !ImageManager.preloadingInProgress){
                    ApplicationLoader.ApplicationIOScope.launch {
                        ImageManager.loadImages(requireContext())
                    }
                } else if(VideoManager.preloadedVideos == null && !VideoManager.preloadingVideosInProgress) {
                    ApplicationLoader.ApplicationIOScope.launch {
                        VideoManager.loadVideos(requireContext())
                    }
                } else if(DocManager.preloadedDocs == null && !DocManager.preloadingInProgress) {
                    ApplicationLoader.ApplicationIOScope.launch {
                        DocManager.loadDocs(requireContext())
                    }
                }
            }

        }

        return binding.root
    }

    fun initAdapter(audioItems: MutableList<AudioItem>) {

        binding.fragmentAudioBrowserList.adapter = AudioBrowserAdapter(requireActivity(), audioItems, layoutInflater)

    }

}