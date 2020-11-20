package pro.filemanager.images

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pro.filemanager.images.albums.ImageAlbumsFragment
import pro.filemanager.images.gallery.ImageGalleryFragment

class ImageBrowserViewModel : ViewModel() {

    private var pagerFragmentsLive: MutableLiveData<MutableList<Fragment>>? = null

    private fun initPagerFragmentsLive() : MutableLiveData<MutableList<Fragment>> {
        if(pagerFragmentsLive == null) {
            pagerFragmentsLive = MutableLiveData(mutableListOf(
                    ImageGalleryFragment(),
                    ImageAlbumsFragment()
            ))
        }

        return pagerFragmentsLive!!
    }

    fun getPagerFragmentsLive() = initPagerFragmentsLive() as LiveData<MutableList<Fragment>>

}