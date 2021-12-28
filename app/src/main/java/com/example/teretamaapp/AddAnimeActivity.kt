package com.example.teretamaapp

import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teretamaapp.room.*
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.serialization.responseObject
import com.github.kittinunf.result.Result
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Resp(var content: String, var author: String)

class AddAnimeActivity : AppCompatActivity(), AnimeSearchAdapter.AnimeSearchListener {
    private var channelId: Int = -1

    private val animeViewModel: AnimeViewModel by viewModels {
        AnimeViewModelFactory((application as TeretamaApplication).repository)
    }

    lateinit var list: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_anime)

        channelId = intent.getIntExtra(CHANNEL_ID, -1)

        // Configure toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Initialize and configure the RecyclerView
        list = findViewById(R.id.anime_results)
        list.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.add_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        val preferredLang = PreferenceManager.getDefaultSharedPreferences(this).getString("title_language", "romaji")

        val queryListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null && !TextUtils.isEmpty(query)) {
                    val body = AnilistRequest(mediaQuery, AnilistVariables(1, 50, query))

                    // Query AniList API
                    // TODO Move it to a different file

                    "https://graphql.anilist.co".httpPost()
                        .header(Headers.CONTENT_TYPE, "application/json;charset=utf-8")
                        .jsonBody(Json.encodeToString(body))
                        .responseObject<AnilistResponse>(json = Json { ignoreUnknownKeys = true }) { _, _, result ->
                            if (result is Result.Success) {
                                val converted = result.get().data.Page.media.map {
                                    val title = when (preferredLang) {
                                        "english" -> it.title.english ?: it.title.romaji
                                        "native" -> it.title.native ?: it.title.romaji
                                        else -> it.title.romaji
                                    }

                                    val studiosString = it.studios.nodes
                                        .filter { node -> node.isAnimationStudio }
                                        .joinToString { node -> node.name }

                                    Anime(channelId, it.id, title ?: "Unknown", it.coverImage.large, it.startDate.year ?: -1, it.episodes ?: 0, studiosString)
                                }

                                val adapter = AnimeSearchAdapter(converted, this@AddAnimeActivity)
                                list.adapter = adapter

                            } else if (result is Result.Failure) {
                                // For development (user won't be able to see it anyway)
                                val ex = result.getException()
                                println(ex)

                                // Show a generic error message to the user. Note: This doesn't happen when there's simply no results.
                                Toast.makeText(this@AddAnimeActivity, R.string.error_generic, Toast.LENGTH_SHORT).show()
                            }
                        }
                }

                return false
            }
        }

        searchView.setOnQueryTextListener(queryListener)

        // Automatically invoke the search bar when user enters the activity for convenience
        menu.performIdentifierAction(R.id.action_search, 0)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onItemSelected(item: Anime) {
        Toast.makeText(this, R.string.add_success, Toast.LENGTH_SHORT).show()

        animeViewModel.insert(item)
        finish()
    }

    companion object {
        const val EXTRA_ID = "com.example.teretamaapp.CHANNEL_ID"
    }
}