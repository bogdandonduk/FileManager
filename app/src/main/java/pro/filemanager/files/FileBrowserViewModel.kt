package pro.filemanager.files

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FileBrowserViewModel() : ViewModel() {

    private var pagerFragmentsLive: MutableLiveData<MutableList<Fragment>>? = null

    var mainRvScrollPosition: Parcelable? = null
}