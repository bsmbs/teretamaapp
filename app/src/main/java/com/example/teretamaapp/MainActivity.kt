package com.example.teretamaapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teretamaapp.room.Channel
import com.example.teretamaapp.room.ChannelViewModel
import com.example.teretamaapp.room.ChannelViewModelFactory
import com.example.teretamaapp.settings.SettingsActivity
import com.example.teretamaapp.settings.themeSet
import com.google.android.material.floatingactionbutton.FloatingActionButton

const val CHANNEL_ID = "com.example.teretamaapp.CHANNEL.ID"
class MainActivity : AppCompatActivity(), CustomAdapter.CustomAdapterListener {
    private lateinit var editResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        // Load configuration
        applyTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.main_toolbar))

        handleAddActivity()
        drawRecycler()
    }

    private val channelViewModel: ChannelViewModel by viewModels {
        ChannelViewModelFactory((application as TeretamaApplication).repository)
    }

    private fun handleAddActivity() {
        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)

        val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                // Add new channel to database
                    activityResult.data?.let { resultData ->
                        val name = resultData.getStringExtra(AddChannelActivity.EXTRA_REPLY)!!
                        val imageUri = resultData.getStringExtra(AddChannelActivity.EXTRA_IMAGEURI)!!

                        val duplicates = channelViewModel.channels.value?.find { it.name == name }
                        if(duplicates == null) {
                            val newChannel = Channel(name, "Added via db", imageUri)
                            channelViewModel.insertAll(newChannel)
                        } else {
                            Toast.makeText(this, R.string.add_error_duplicate, Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        fab.setOnClickListener {
            val intent = Intent(this, AddChannelActivity::class.java)

            startForResult.launch(intent)
        }
    }

    override fun onItemEdit(item: Channel) {
        val intent = Intent(this, AddChannelActivity::class.java)

        val bundle = Bundle()
        bundle.putInt("id", item.id)
        bundle.putString("name", item.name)
        bundle.putString("imageUri", item.imageUri)

        intent.putExtra("edit", bundle)
        editResult.launch(intent)
    }

    private fun drawRecycler() {
        val recyclerview = findViewById<RecyclerView>(R.id.recycler)

        recyclerview.layoutManager = LinearLayoutManager(this)

        // Channel was edited
        editResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                activityResult.data?.let { resultData ->
                    val id = resultData.getIntExtra(AddChannelActivity.EXTRA_ID, -1)

                    val entry = channelViewModel.channels.value?.find { it.id == id }
                    if (entry != null && id >= 0) {
                        val name = resultData.getStringExtra(AddChannelActivity.EXTRA_REPLY)!!
                        val imageUri = resultData.getStringExtra(AddChannelActivity.EXTRA_IMAGEURI)!!

                        entry.name = name
                        entry.imageUri = imageUri

                        channelViewModel.update(entry)
                    }
                }
            }

        }

        channelViewModel.channels.observe(this, { channels ->
            if (channels != null) {
                val adapter = CustomAdapter(channels, this, channelViewModel)
                recyclerview.adapter = adapter
            }
        })
    }

    /**
     * Set app theme according to current preference.
     */
    private fun applyTheme() {
        // Get current value
        val theme = PreferenceManager.getDefaultSharedPreferences(this).getString("theme", "default")
        // Set theme
        themeSet(theme)
    }

    // Setup the toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.action_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }
}