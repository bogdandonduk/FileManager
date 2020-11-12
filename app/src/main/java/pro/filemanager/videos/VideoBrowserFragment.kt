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
import pro.filemanager.files.FileManager
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
                val videoItems: MutableList<VideoItem> = if(VideoManager.loadedVideos != null) {

                    VideoManager.loadedVideos!!
                } else {
                    if(!VideoManager.loadingInProgress) {
                        VideoManager.loadVideos(requireContext())
                        VideoManager.loadedVideos!!
                    } else {
                        while(VideoManager.loadingInProgress && VideoManager.loadedVideos == null) {
                            delay(25)
                        }

                        VideoManager.loadedVideos!!
                    }
                }

                withContext(Main) {
                    initAdapter(videoItems)

                    binding.fragmentVideoBrowserList.layoutManager = GridLayoutManager(context, UIManager.getImageBrowserSpanNumber(requireActivity()))

                    if(savedInstanceState?.getParcelable<Parcelable>("rvScrollPosition") != null)
                        binding.fragmentVideoBrowserList.layoutManager?.onRestoreInstanceState(savedInstanceState.getParcelable("rvScrollPosition"))
                }
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        if(ImageManager.loadedImages == null && !ImageManager.loadingInProgress){
            ApplicationLoader.loadImages()
        } else if(AudioManager.loadedAudios == null && !AudioManager.loadingInProgress) {
            ApplicationLoader.loadAudios()
        } else if(FileManager.externalRootPath == null && !FileManager.findingExternalRootInProgress) {
            ApplicationLoader.findExternalRoot()
        } else if(DocManager.loadedDocs == null && !DocManager.loadingInProgress) {
            ApplicationLoader.loadDocs()
        }
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