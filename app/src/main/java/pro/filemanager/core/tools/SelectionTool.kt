package pro.filemanager.core.tools

import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.annotation.IntDef
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity

class SelectionTool {
    companion object {
        const val CLICK_SHORT = 1
        const val CLICK_LONG = 2

        @IntDef(CLICK_SHORT, CLICK_LONG)
        @Retention(AnnotationRetention.SOURCE)
        annotation class ClickType
    }

    var selectionMode = false
    val selectedPaths = mutableListOf<String>()

    fun handleClickInViewHolder(
            @ClickType clickType: Int,
            position: Int,
            path: String,
            adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
            offAction: Runnable = Runnable {}
    ) {
        if(clickType == CLICK_SHORT) {
            if(!selectionMode)
                offAction.run()
            else {
                if(!selectedPaths.contains(path)) {
                    selectedPaths.add(path)
                } else {
                    selectedPaths.remove(path)
                }

                adapter.notifyItemChanged(position)
            }
        } else if(clickType == CLICK_LONG){
            if(!selectionMode) {
                selectionMode = true
                selectedPaths.add(path)

                for (i in 0 until adapter.itemCount) {
                    ApplicationLoader.ApplicationMainScope.launch {
                        adapter.notifyItemChanged(i)
                    }
                }
            } else {
                if(!selectedPaths.contains(path)) {
                    selectedPaths.add(path)
                } else {
                    selectedPaths.remove(path)
                }
            }
        }
    }

    fun selectAll(items: MutableList<String>, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>, scope: CoroutineScope?) {
        selectedPaths.clear()
        selectedPaths.addAll(items)

        for (i in 0 until adapter.itemCount) {
            scope?.launch {
                adapter.notifyItemChanged(i)
            }
        }

    }

    fun unselectAll(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>, scope: CoroutineScope?) {
        selectedPaths.clear()

        for (i in 0 until adapter.itemCount) {
            scope?.launch {
                adapter.notifyItemChanged(i)
            }
        }
    }

    fun initOnBackCallback(
            activity: HomeActivity,
            adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
            selectionCheckBox: CheckBox,
            selectionCheckBoxLayout: ViewGroup,
            toolbarLayout: ViewGroup,
            tabsBarLayout: ViewGroup
    ) {
         activity.currentOnBackBehavior = if(selectionMode)
             Runnable {
                 selectedPaths.clear()
                 selectionMode = false

                 for (i in 0 until adapter.itemCount) {
                     ApplicationLoader.ApplicationMainScope.launch {
                         adapter.notifyItemChanged(i)
                     }
                 }

                 if(selectionCheckBox.isChecked)
                     selectionCheckBox.toggle()

                 selectionCheckBoxLayout.visibility = View.GONE
                 activity.supportActionBar?.show()

                 toolbarLayout.visibility = View.GONE
                 tabsBarLayout.visibility = View.VISIBLE

                 activity.currentOnBackBehavior = null
             }

         else null
    }

    fun differentiateItem(
            path: String,
            thumbnail: ImageView,
            checkMark: ImageView,
            uncheckedMark: ImageView,
    ) {
        if(selectionMode) {
            if(selectedPaths.contains(path)) {
                checkMark.visibility = View.VISIBLE
                uncheckedMark.visibility = View.INVISIBLE

                thumbnail.setColorFilter(Color.argb(100, 0, 0, 0))

                checkMark.scaleX = 0f
                checkMark.scaleY = 0f

                checkMark.visibility = View.VISIBLE
                checkMark.animate().scaleX(1f).setDuration(150).start()
                checkMark.animate().scaleY(1f).setDuration(150).start()
            } else {
                thumbnail.colorFilter = null

                checkMark.visibility = View.INVISIBLE
                uncheckedMark.visibility = View.VISIBLE
            }
        } else {
            thumbnail.colorFilter = null

            checkMark.visibility = View.INVISIBLE
            uncheckedMark.visibility = View.INVISIBLE
        }
    }

    fun initSelectionState(
            activity: HomeActivity,
            adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
            toolbarLayout: ViewGroup,
            tabsBarLayout: ViewGroup,
            selectionCheckBoxLayout: ViewGroup,
            selectionCheckBox: CheckBox,
            selectionMode: Boolean,
            selectedItemsCount: Int
    ) {
        if(selectionMode) {
            if(activity.currentOnBackBehavior == null) initOnBackCallback(activity, adapter, selectionCheckBox, selectionCheckBoxLayout, toolbarLayout, tabsBarLayout)

            if(activity.supportActionBar != null && activity.supportActionBar!!.isShowing) activity.supportActionBar!!.hide()

            if(selectionCheckBoxLayout.visibility != View.VISIBLE) selectionCheckBoxLayout.visibility = View.VISIBLE

            selectionCheckBox.text = selectedItemsCount.toString()

            if(tabsBarLayout.visibility == View.VISIBLE) tabsBarLayout.visibility = View.GONE
            if(toolbarLayout.visibility != View.VISIBLE) toolbarLayout.visibility = View.VISIBLE
        } else {
            if(activity.supportActionBar != null && !activity.supportActionBar!!.isShowing) activity.supportActionBar!!.show()

            if(selectionCheckBoxLayout.visibility == View.VISIBLE) selectionCheckBoxLayout.visibility = View.GONE

            if(tabsBarLayout.visibility != View.VISIBLE) tabsBarLayout.visibility = View.VISIBLE
            if(toolbarLayout.visibility == View.VISIBLE) toolbarLayout.visibility = View.GONE
        }
    }
}