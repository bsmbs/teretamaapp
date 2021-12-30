package com.example.teretamaapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.net.toFile
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.teretamaapp.room.Channel
import com.example.teretamaapp.room.ChannelViewModel
import java.io.FileNotFoundException

// CustomAdapter for RecyclerView with channel list
class CustomAdapter(private val mList: List<Channel>, private var mContext: Context, private val mChannelViewModel: ChannelViewModel) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
    internal lateinit var listener: CustomAdapterListener

    interface CustomAdapterListener {
        fun onItemEdit(item: Channel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_design, parent, false)

        try {
            listener = mContext as CustomAdapterListener
        } catch (e: Exception) { }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemsViewModel = mList[position]

        holder.imageView.setImageResource(R.drawable.tokyo_mx)
        holder.imageView.setColorFilter(0xffffff)

        holder.imageView.load(Uri.parse(itemsViewModel.imageUri))

        holder.id = itemsViewModel.id
        holder.textView.text = itemsViewModel.name
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun openChannelActivity(channelId: Int) {
        val intent = Intent(mContext, ChannelActivity::class.java).apply {
            putExtra(CHANNEL_ID, channelId)
        }

        mContext.startActivity(intent)
    }

    inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView), View.OnCreateContextMenuListener {
        val cardView: CardView = itemView.findViewById(R.id.cardview)
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val textView: TextView = itemView.findViewById(R.id.textview)
        var id: Int? = null

        init {
            itemView.setOnCreateContextMenuListener(this)
            cardView.setOnClickListener { id?.let { id -> openChannelActivity(id) } }
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            // Find the selected channel in database
            val entry = mChannelViewModel.channels.value?.find { it.id == id }

            menu!!.add(0, v!!.id, 1, R.string.item_remove).setOnMenuItemClickListener {
                // Delete it
                if (entry != null) {
                    mChannelViewModel.delete(entry)
                    try {
                        val uri = Uri.parse(entry.imageUri)
                        if (uri.scheme == "file") {
                            uri.toFile().delete()
                        }
                    } catch (e: FileNotFoundException) { }
                }

                // Show a message to user
                Toast.makeText(mContext, R.string.item_removed, Toast.LENGTH_SHORT).show()
                return@setOnMenuItemClickListener true
            }
            menu.add(0, v.id, 0, R.string.item_edit).setOnMenuItemClickListener {

                if(entry != null) {
                    listener.onItemEdit(entry)
                }

                return@setOnMenuItemClickListener true
            }
        }
    }
}