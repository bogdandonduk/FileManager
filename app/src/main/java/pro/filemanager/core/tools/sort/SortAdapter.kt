package pro.filemanager.core.tools.sort

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pro.filemanager.databinding.LayoutSortItemBinding

class SortAdapter(val context: Context, val sortOptionItems: MutableList<SortOptionItem>, val layoutInflater: LayoutInflater) : RecyclerView.Adapter<SortAdapter.SortBottomModalSheetItemViewHolder>() {
    class SortBottomModalSheetItemViewHolder(val binding: LayoutSortItemBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var sortOptionItem: SortOptionItem

        init {
            binding.layoutSortBottomModalSheetItemRootLayout.setOnClickListener {
                if(this::sortOptionItem.isInitialized)
                    sortOptionItem.action.invoke()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SortBottomModalSheetItemViewHolder =
            SortBottomModalSheetItemViewHolder(LayoutSortItemBinding.inflate(layoutInflater, parent, false))

    override fun onBindViewHolder(holder: SortBottomModalSheetItemViewHolder, position: Int) {
        holder.sortOptionItem = sortOptionItems[position]

        holder.binding.layoutSortBottomModalSheetItemRootLayout.post {
            holder.binding.layoutSortBottomModalSheetItemTitle.textSize = (holder.binding.layoutSortBottomModalSheetItemRootLayout.height / 8).toFloat()
        }

        holder.binding.layoutSortBottomModalSheetItemTitle.text = holder.sortOptionItem.title

        if(SortTool.lastViewModel != null && holder.sortOptionItem.value == SortTool.lastViewModel!!.currentSortOrder)
            holder.binding.layoutSortBottomModalSheetItemCheckIcon.visibility = View.VISIBLE
        else
            holder.binding.layoutSortBottomModalSheetItemCheckIcon.visibility = View.INVISIBLE

    }

    override fun getItemCount(): Int = sortOptionItems.size
}