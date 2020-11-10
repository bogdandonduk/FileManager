package pro.filemanager.images

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity
import pro.filemanager.audios.AudioManager
import pro.filemanager.core.UIManager
import pro.filemanager.databinding.FragmentImageBrowserBinding
import pro.filemanager.docs.DocManager
import pro.filemanager.videos.VideoManager

class ImageBrowserFragment() : Fragment() {

    lateinit var binding: FragmentImageBrowserBinding
    lateinit var navController: NavController
    lateinit var IOScope: CoroutineScope
    lateinit var MainScope: CoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        IOScope = CoroutineScope(IO)
        MainScope = CoroutineScope(Main)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImageBrowserBinding.inflate(inflater, container, false)

        (requireActivity() as HomeActivity).requestExternalStoragePermission {
            ApplicationLoader.ApplicationIOScope.launch {
                val imageItems: MutableList<ImageItem> = if(ImageManager.preloadedImages != null) {

                    ImageManager.preloadedImages!!
                } else {
                    if(!ImageManager.preloadingInProgress) {
                        ImageManager.preloadImages(requireContext())
                        ImageManager.preloadedImages!!
                    } else {
                        while(ImageManager.preloadingInProgress && ImageManager.preloadedImages == null) {
                            delay(25)
                        }

                        ImageManager.preloadedImages!!
                    }
                }

                withContext(Main) {
                    initAdapter(imageItems)

                    binding.fragmentImageBrowserList.layoutManager = GridLayoutManager(context, UIManager.getImageBrowserSpanNumber(requireActivity()))

                    if(savedInstanceState?.getParcelable<Parcelable>("rvScrollPosition") != null)
                        binding.fragmentImageBrowserList.layoutManager?.onRestoreInstanceState(savedInstanceState.getParcelable("rvScrollPosition"))
                }

                if(VideoManager.preloadedVideos == null && !VideoManager.preloadingVideosInProgress){
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable("rvScrollPosition", binding.fragmentImageBrowserList.layoutManager?.onSaveInstanceState())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
    }

    fun initAdapter(imageItems: MutableList<ImageItem>) {
        binding.fragmentImageBrowserList.adapter = ImageBrowserAdapter(requireActivity(), imageItems, layoutInflater, this@ImageBrowserFragment)
    }

    override fun onDestroy() {
        super.onDestroy()

        IOScope.cancel()
        MainScope.cancel()

    }
}