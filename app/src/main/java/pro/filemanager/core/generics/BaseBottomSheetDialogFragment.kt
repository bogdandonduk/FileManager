package pro.filemanager.core.generics

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity

open class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    open lateinit var frContext: Context
    open lateinit var activity: HomeActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = requireActivity() as HomeActivity

        frContext =
                try {
                    requireContext()
                } catch(thr: Throwable) {
                    activity.applicationContext
                } finally {
                    ApplicationLoader.appContext
                }
    }

}