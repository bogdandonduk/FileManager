package pro.filemanager.core.tools

import android.util.Log
import androidx.annotation.IntDef
import androidx.recyclerview.widget.RecyclerView
import pro.filemanager.HomeActivity

class SelectorTool {
    companion object {
        const val CLICK_SHORT = 1
        const val CLICK_LONG = 2

        @IntDef(CLICK_SHORT, CLICK_LONG)
        @Retention(AnnotationRetention.SOURCE)
        annotation class ClickType
    }

    private var selectionMode = false
    val selectedPositions = mutableListOf<Int>()

    fun handleClickInViewHolder(@ClickType clickType: Int, position: Int, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>, activity: HomeActivity, offAction: Runnable = Runnable {}) {
        if(clickType == CLICK_SHORT) {

            if(!selectionMode)
                offAction.run()
            else {
                if(!selectedPositions.contains(position)) {
                    selectedPositions.add(position)
                    adapter.notifyItemChanged(position)
                } else {
                    selectedPositions.remove(position)
                    adapter.notifyItemChanged(position)

                    if(selectedPositions.isEmpty()) {
                        selectionMode = false

                        assignOnBackBehavior(activity, adapter)
                    }

                }

            }
        } else if(clickType == CLICK_LONG){
            if(!selectionMode) {
                selectionMode = true
                selectedPositions.add(position)

                adapter.notifyItemChanged(position)
                assignOnBackBehavior(activity, adapter)
            } else {
                if(!selectedPositions.contains(position)) {
                    selectedPositions.add(position)
                    adapter.notifyItemChanged(position)
                } else {
                    selectedPositions.remove(position)
                    adapter.notifyItemChanged(position)

                    if(selectedPositions.isEmpty()) {
                        selectionMode = false

                        assignOnBackBehavior(activity, adapter)
                    }
                }
            }
         }
    }

    fun assignOnBackBehavior(activity: HomeActivity, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        activity.onBackBehavior = if(selectionMode) {

            Runnable {
                val copy = mutableListOf<Int>()

                copy.addAll(selectedPositions)

                selectedPositions.clear()
                selectionMode = false

                copy.forEach {
                    adapter.notifyItemChanged(it)
                }

                activity.onBackBehavior = null
            }
        } else {
            null
        }
    }
}