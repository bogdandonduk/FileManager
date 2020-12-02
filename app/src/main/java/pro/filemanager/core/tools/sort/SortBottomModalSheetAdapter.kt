package pro.filemanager.core.tools.sort

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pro.filemanager.databinding.LayoutSortBottomModalSheetItemBinding

class SortBottomModalSheetAdapter(val context: Context, val optionItems: MutableList<OptionItem>, val layoutInflater: LayoutInflater) : RecyclerView.Adapter<SortBottomModalSheetAdapter.SortBottomModalSheetItemViewHolder>() {
    class SortBottomModalSheetItemViewHolder(val binding: LayoutSortBottomModalSheetItemBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var optionItem: OptionItem

        init {
            binding.layoutSortBottomModalSheetItemRootLayout.setOnClickListener {
                if(this::optionItem.isInitialized)
                    optionItem.action.run()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SortBottomModalSheetItemViewHolder =
            SortBottomModalSheetItemViewHolder(LayoutSortBottomModalSheetItemBinding.inflate(layoutInflater, parent, false))

    override fun onBindViewHolder(holder: SortBottomModalSheetItemViewHolder, position: Int) {
        holder.optionItem = optionItems[position]

        holder.binding.layoutSortBottomModalSheetItemTitle.text = holder.optionItem.title

    }

    override fun getItemCount(): Int = optionItems.size
}