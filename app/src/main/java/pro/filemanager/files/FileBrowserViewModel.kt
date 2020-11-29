package pro.filemanager.files

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FileBrowserViewModel(val path: String) : ViewModel() {
    var mainListRvState: Parcelable? = null
}