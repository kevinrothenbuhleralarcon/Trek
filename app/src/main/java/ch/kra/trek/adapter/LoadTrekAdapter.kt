package ch.kra.trek.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.kra.trek.database.Trek
import ch.kra.trek.databinding.TrekListItemBinding

class LoadTrekAdapter(private val onclick:(Int) -> Unit): ListAdapter<Trek, LoadTrekAdapter.LoadTrekViewHolder>(DiffCallBack) {

    companion object {
        private val DiffCallBack = object: DiffUtil.ItemCallback<Trek>() {
            override fun areItemsTheSame(oldItem: Trek, newItem: Trek): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Trek, newItem: Trek): Boolean {
                return oldItem == newItem
            }
        }
    }

    class LoadTrekViewHolder(val binding: TrekListItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(trek: Trek) {
            binding.trek = trek
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