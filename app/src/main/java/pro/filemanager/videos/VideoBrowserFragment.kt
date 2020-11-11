package pro.filemanager.videos

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity
import pro.filemanager.audios.AudioManager
import pro.filemanager.core.UIManager
import pro.filemanager.databinding.FragmentVideoBrowserBinding
import pro.filemanager.docs.DocManager
import pro.filemanager.images.ImageManager

class VideoBrowserFragment() : Fragment() {

    lateinit var binding: FragmentVideoBrowserBinding

    val IOScope = CoroutineScope(IO)
    val MainScope = CoroutineScope(Main)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVideoBrowserBinding.inflate(inflater, container, false)

        binding.fragmentVideoBrowserList.layoutManager = GridLayoutManager(context, 4)

        (requireActivity() as HomeActivity).requestExternalStoragePermission {
            ApplicationLoader.ApplicationIOScope.launch {
                val videoItems: MutableList<VideoItem> = if(VideoManager.preloadedVideos != null) {

                    VideoManager.preloadedVideos!!
                } else {
                    if(!VideoManager.preloadingVideosInProgress) {
                        VideoManager.loadVideos(requireContext())
                        VideoManager.preloadedVideos!!
                    } else {
                        while(VideoManager.preloadingVideosInProgress && VideoManager.preloadedVideos == null) {
                            delay(25)
                        }

                        VideoManager.preloadedVideos!!
                    }
                }

                withContext(Main) {
                    initAdapter(videoItems)

                    binding.fragmentVideoBrowserList.layoutManager = GridLayoutManager(context, UIManager.getImageBrowserSpanNumber(requireActivity()))

                    if(savedInstanceState?.getParcelable<Parcelable>("rvScrollPosition") != null)
                        binding.fragmentVideoBrowserList.layoutManager?.onRestoreInstanceState(savedInstanceState.getParcelable("rvScrollPosition"))
                }

                if(ImageManager.preloadedImages == null && !ImageManager.preloadingInProgress){
                    ApplicationLoader.ApplicationIOScope.launch {
                        ImageManager.loadImages(requireContext())
                    }
                } else if(DocManager.preloadedDocs == null && !DocManager.preloadingInProgress) {
                    ApplicationLoader.ApplicationIOScope.launch {
                        DocManager.loadDocs(requireContext())
                    }
                } else if(AudioManager.preloadedAudios == null && !AudioManager.preloadingInProgress) {
                    ApplicationLoader.ApplicationIOScope.launch {
                        AudioManager.loadAudios(requireContext())
                    }
                }

            }
        }

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable("rvScrollPosition", binding.fragmentVideoBrowserList.layoutManager?.onSaveInstanceState())
    }

    fun initAdapter(videoItems: MutableList<VideoItem>) {
        binding.fragmentVideoBrowserList.adapter = VideoBrowserAdapter(requireActivity(), videoItems, layoutInflater, this@VideoBrowserFragment)
    }

    override fun onDestroy() {
        super.onDestroy()

        IOScope.cancel()
        MainScope.cancel()
    }
}