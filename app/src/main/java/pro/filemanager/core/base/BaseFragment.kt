package pro.filemanager.core.base

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity

open class BaseFragment : Fragment() {

    open lateinit var frContext: Context
    open lateinit var activity: HomeActivity
    open lateinit var navController: NavController

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