package com.example.teretamaapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.teretamaapp.room.Anime
import com.google.android.material.card.MaterialCardView

class AnimeSearchAdapter(private val mList: List<Anime>, private var mContext: Context) : RecyclerView.Adapter<AnimeSearchAdapter.ViewHolder>() {
    internal lateinit var listener: AnimeSearchListener


    interface AnimeSearchListener {
        fun onItemSelected(item: Anime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.anime_view_design_list, parent, false)

        // Scale down
        val coverContainer = view.findViewById<MaterialCardView>(R.id.anime_cover_container)
        val coverImage = view.findViewById<ImageView>(R.id.anime_cover)

        val resources = parent.resources

        coverContainer.layoutParams.width = resources.getDimensionPixelSize(R.dimen.list_mini_width)
        coverContainer.layoutParams.height = resources.getDimensionPixelSize(R.dimen.list_mini_container_height)

        coverImage.layoutParams.width = resources.getDimensionPixelSize(R.dimen.list_mini_width)
        coverImage.layoutParams.height = resources.getDimensionPixelSize(R.dimen.list_mini_height)
        coverImage.scaleType = ImageView.ScaleType.CENTER_CROP

        try {
            listener = mContext as AnimeSearchListener
        } catch (e: Exception) { }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemViewModel = mList[position]

        holder.imageView.load(itemViewModel.imageUri)
        holder.item = itemViewModel
        holder.textView.text = itemViewModel.title
        holder.details.text = mContext.getString(R.string.anime_details, itemViewModel.releaseYear, itemViewModel.studios, itemViewModel.episodeCount)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val container: CardView = itemView.findViewById(R.id.anime_container)
        val imageView: ImageView = itemView.findViewById(R.id.anime_cover)
        val textView: TextView = itemView.findViewById(R.id.anime_title)
        val details: TextView = itemView.findViewById(R.id.anime_details)

        lateinit var item: Anime

        init {
            container.setOnClickListener {
                listener.onItemSelected(item)
            }
        }
    }
}