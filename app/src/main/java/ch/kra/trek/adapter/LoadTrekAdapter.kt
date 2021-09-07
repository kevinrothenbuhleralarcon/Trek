package ch.kra.trek.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.kra.trek.database.TrekData
import ch.kra.trek.databinding.TrekListItemBinding

class LoadTrekAdapter(private val onclick:(Int) -> Unit): ListAdapter<TrekData, LoadTrekAdapter.LoadTrekViewHolder>(DiffCallBack) {

    companion object {
        private val DiffCallBack = object: DiffUtil.ItemCallback<TrekData>() {
            override fun areItemsTheSame(oldItem: TrekData, newItem: TrekData): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TrekData, newItem: TrekData): Boolean {
                return oldItem == newItem
            }
        }
    }

    class LoadTrekViewHolder(val binding: TrekListItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(trekData: TrekData) {
            binding.trek = trekData
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoadTrekViewHolder {
        val loadTrekViewHolder = LoadTrekViewHolder(TrekListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        loadTrekViewHolder.binding.btnLoadTrek.setOnClickListener { onclick(getItem(loadTrekViewHolder.adapterPosition).id) }
        return loadTrekViewHolder
    }

    override fun onBindViewHolder(holder: LoadTrekViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}