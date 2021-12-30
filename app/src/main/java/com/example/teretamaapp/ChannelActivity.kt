package com.example.teretamaapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teretamaapp.room.*
import com.google.android.material.floatingactionbutton.FloatingActionButton

val tabArray = arrayOf(
    "General",
    "List"
)

class ChannelActivity : AppCompatActivity() {
    lateinit var adapter: AnimeAdapter
    lateinit var pref: SharedPreferences

    private var channelId: Int = -1

    private val animeViewModel: AnimeViewModel by viewModels {
        AnimeViewModelFactory((application as TeretamaApplication).repository)
    }

    private val channelViewModel: ChannelViewModel by viewModels {
        ChannelViewModelFactory((application as TeretamaApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel)

        channelId = intent.getIntExtra(CHANNEL_ID, -1)

        // Setup toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_baseline_close_24)

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // add button
        handleAdd()
        handleSort()

        val emptyMessage = findViewById<TextView>(R.id.anime_empty)

        // recyclerview
        val list = findViewById<RecyclerView>(R.id.anime_list)

        list.layoutManager = LinearLayoutManager(this)

        // Set current sort
        animeViewModel.setSort(Sort.values()[pref.getInt("sort", Sort.DEFAULT.ordinal)])

        channelViewModel.channels.observe(this, { channels ->
            val channelEntry = channels.find { it.id == channelId }
            if (channelEntry != null) {
                supportActionBar?.title = channelEntry.name

                animeViewModel.setChannel(channelId)
                animeViewModel.sortedAnime.observe(this, { anime ->
                    if (anime.isEmpty()) {
                        emptyMessage.visibility = View.VISIBLE
                    } else {
                        emptyMessage.visibility = View.GONE
                    }

                    adapter = AnimeAdapter(anime, this, animeViewModel, channels)
                    list.adapter = adapter

                    val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = true
                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            adapter.notifyItemChanged(viewHolder.layoutPosition)
                            viewHolder.itemView.showContextMenu()
                        }
                    }

                    val itemTouchHelper = ItemTouchHelper(callback)
                    itemTouchHelper.attachToRecyclerView(list)
                })
            } else {
                onBackPressed()
                Toast.makeText(this, "Something's gone wrong", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleAdd() {
        val fab = findViewById<FloatingActionButton>(R.id.anime_add)

        val act = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { _ ->
                    adapter.notifyItemInserted(adapter.itemCount)
                }
            }
        }

        fab.setOnClickListener {
            val intent = Intent(this, AddAnimeActivity::class.java)
            intent.putExtra(CHANNEL_ID, channelId)

            act.launch(intent)
        }
    }

    private fun handleSort() {
        val fab = findViewById<FloatingActionButton>(R.id.anime_sort)

        fab.setOnClickListener {
            val currentSort = pref.getInt("sort", Sort.DEFAULT.ordinal)

            // Show selection dialog
            val builder = AlertDialog.Builder(this)

            builder.setTitle(R.string.sort)
                .setSingleChoiceItems(R.array.sort, currentSort) { dialog, which ->
                    val newSort = Sort.values()[which]

                    /*val newSort = when (which) {
                        1 -> Sort.TITLE
                        2 -> Sort.YEAR
                        else -> Sort.DEFAULT
                    }*/

                    // Save to preferences
                    pref.edit().putInt("sort", which).apply()

                    // Apply changes to recycler
                    animeViewModel.setSort(newSort)

                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }

            builder.show()
        }
    }
}