package pro.filemanager.core.base

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import pro.filemanager.ApplicationLoader
import pro.filemanager.R
import pro.filemanager.home.HomeActivity

open class BaseBottomModalSheetFragment : BottomSheetDialogFragment() {

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

    override fun onCancel(dialog: DialogInterface) {
        dismiss()
    }

    fun expandSheet() {
        dialog?.setOnShowListener {
            BottomSheetBehavior.from(dialog!!.findViewById<FrameLayout>(R.id.design_bottom_sheet)!!).state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    fun transparifyBackground() {
        (view?.parent as View).run {
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(5, 0, 5, 0)
        }

    }
}