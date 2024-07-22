package campus.tech.kakao.map.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import campus.tech.kakao.map.data.Keyword
import campus.tech.kakao.map.databinding.ItemSavedKeywordBinding

class SavedKeywordsAdapter(
    private var keywords: List<Keyword>,
    private val onDeleteClick: (Keyword) -> Unit,
    private val onKewordClick: (Keyword) -> Unit
) : RecyclerView.Adapter<SavedKeywordsAdapter.SavedKeywordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedKeywordViewHolder {
        val binding = ItemSavedKeywordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SavedKeywordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SavedKeywordViewHolder, position: Int) {
        holder.bind(keywords[position])
    }

    override fun getItemCount(): Int = keywords.size

    inner class SavedKeywordViewHolder(private val binding: ItemSavedKeywordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(keyword: Keyword) {
            binding.keyword = keyword
            binding.btnKeyword.setOnClickListener { onKewordClick(keyword) }
            binding.btnDelete.setOnClickListener { onDeleteClick(keyword) }
        }
    }

    fun updateKeywords(newKeywords: List<Keyword>) {
        this.keywords = newKeywords
        notifyDataSetChanged()
    }
}