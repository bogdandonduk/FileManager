package pro.filemanager.core.base

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity
import pro.filemanager.R
import pro.filemanager.core.PermissionWrapper
import pro.filemanager.core.tools.SelectionTool
import java.util.function.BiConsumer

open class BaseSectionFragment : Fragment() {

    open lateinit var frContext: Context
    open lateinit var activity: HomeActivity
    open lateinit var navController: NavController
    open lateinit var stabilizingToast: Toast // temporary

    @Volatile var shouldUseDiffUtil = false
    @Volatile var shouldScrollToTop = true

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

        stabilizingToast = Toast.makeText(frContext, "Stabilizing", Toast.LENGTH_SHORT)
    }

    open fun launchCore() {

    }

    open fun notifyListEmpty(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>, text: TextView) {
        if(adapter.itemCount == 0)
            text.visibility = View.VISIBLE
        else
            text.visibility = View.GONE
    }

    open fun initTabsBar(
            tabsBarLayout: ViewGroup,
            highlightTitle: TextView,
            indicator: ViewGroup,
            libraryLayout: ViewGroup,
            libraryTitle: TextView,
            libraryText: String,
            libraryOnClickListener: View.OnClickListener,
            foldersLayout: ViewGroup,
            foldersTitle: TextView,
            foldersOnClickListener: View.OnClickListener,
    ) {
        tabsBarLayout.post {
            tabsBarLayout.height.let {
                libraryTitle.textSize = (it / 8).toFloat()
                libraryTitle.text = libraryText

                foldersTitle.textSize = (it / 8).toFloat()
                foldersTitle.text = resources.getText(R.string.title_folders)
            }

            highlightTitle.setTypeface(null, Typeface.BOLD)
            indicator.visibility = View.VISIBLE
        }

        libraryLayout.setOnClickListener(libraryOnClickListener)
        foldersLayout.setOnClickListener(foldersOnClickListener)
    }

    open fun initSelectionState(
            viewModel: BaseViewModel,
            activity: HomeActivity,
            adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
            toolBarLayout: ViewGroup,
            tabsBarLayout: ViewGroup,
            selectionBarLayout: ViewGroup,
            selectionCheckBox: CheckBox
    ) {
        viewModel.selectionTool.initSelectionState(
                activity,
                adapter,
                toolBarLayout,
                tabsBarLayout,
                selectionBarLayout,
                selectionCheckBox,
                viewModel.selectionTool.selectionMode,
                viewModel.selectionTool.selectedPaths.size
        )
    }

    open fun initSelectionBar(
            selectionCheckBox: CheckBox,
            selectionCheckBoxOnCheckedChangeListener: CompoundButton.OnCheckedChangeListener
    ) {
        selectionCheckBox.setOnCheckedChangeListener(selectionCheckBoxOnCheckedChangeListener)
    }

    open fun initToolBar(
            toolBarLayout: ViewGroup,
            actionButtonLayouts: MutableList<ViewGroup>,
            actionButtonOnClickListeners: MutableList<View.OnClickListener>,
            actionButtonTitles: MutableList<TextView>
    ) {
        toolBarLayout.post {
            toolBarLayout.height.let { height ->
                actionButtonTitles.forEach {
                    it.textSize = (height / 12).toFloat()
                }
            }

            toolBarLayout.visibility = View.GONE
        }

        actionButtonLayouts.forEachIndexed { i: Int, actionButtonLayout: ViewGroup ->
            actionButtonLayout.setOnClickListener(actionButtonOnClickListeners[i])
        }
    }

    fun handleUserReturnFromAppSettings(activity: HomeActivity) {
        if(ApplicationLoader.isUserSentToAppDetailsSettings && PermissionWrapper.checkExternalStoragePermissions(frContext)) {
            launchCore()
            ApplicationLoader.isUserSentToAppDetailsSettings = false
        } else if(ApplicationLoader.isUserSentToAppDetailsSettings && !PermissionWrapper.checkExternalStoragePermissions(frContext)) {
            ApplicationLoader.isUserSentToAppDetailsSettings = false
            activity.onBackPressed()
        }
    }

    fun setAppBarTitle(activity: HomeActivity, title: String) {
        activity.supportActionBar?.title = title
    }
}