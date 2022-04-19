package com.blondo.mymusicdatabase

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import java.sql.RowId
import kotlin.concurrent.thread

class ViewMusic : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_music)
        thread { load() }
    }

    fun cancel(view: View) {
        finish()
    }

    fun load() {

        //controls
        var inReleaseName = findViewById<EditText>(R.id.input_release_txt)
        var inArtists = findViewById<EditText>(R.id.input_artist_txt)
        var inGenres = findViewById<EditText>(R.id.input_genre_txt)
        var inReleaseType = findViewById<EditText>(R.id.input_releaseType_txt)
        var inLink = findViewById<EditText>(R.id.input_link_txt)
        var inReleaseDate = findViewById<EditText>(R.id.input_releaseDate_txt)
        var inDiscoveryDate = findViewById<EditText>(R.id.input_discoveryDate_txt)
        var inNotes = findViewById<EditText>(R.id.input_notes_txt)

        //database
        val music: MusicDao = AppDatabase.getInstance(applicationContext!!)?.musicDao()!!
        val musicID = getIntent().getExtras()!!.getInt("id")
        val thisRelease = music.getFromId(musicID)

        val releaseName = thisRelease.name
        val artists = music.getArtistsWithMusicId(musicID)
        val genres = music.getGenresWithMusicId(musicID)
        val releaseType = thisRelease.releaseType
        val link = thisRelease.link
        val releaseDate = thisRelease.releaseDate
        val discoveryDate = thisRelease.discoveryDate
        val notes = thisRelease.notes

        //accesses ui ONLY on the ui thread
        runOnUiThread {
            inReleaseName.setText(releaseName)
            inArtists.setText(artists)
            inGenres.setText(genres)
            inReleaseType.setText(releaseType)
            inLink.setText(link)
            inReleaseDate.setText(releaseDate)
            inDiscoveryDate.setText(discoveryDate)
            inNotes.setText(notes)
        }
    }

    fun link(view: View) {

        thread {
            //database
            val music: MusicDao = AppDatabase.getInstance(applicationContext!!)?.musicDao()!!
            val musicID = getIntent().getExtras()!!.getInt("id")
            val thisRelease = music.getFromId(musicID)
            val link = thisRelease.link

            val uri: Uri = Uri.parse(link)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        finish()
    }

    fun delete(view: View) {

        thread {
            val music: MusicDao = AppDatabase.getInstance(applicationContext!!)?.musicDao()!!
            val musicID = getIntent().getExtras()!!.getInt("id")

            music.updateArtists(musicID, emptyList())
            music.updateGenres(musicID, emptyList())

            music.delete(music.getFromId(musicID))
        }

        finish()
    }

    fun update(view: View) {

        //inputs
        var inReleaseName = findViewById<EditText>(R.id.input_release_txt).text.toString()
        var inReleaseType = findViewById<EditText>(R.id.input_releaseType_txt).text.toString()
        var inLink = findViewById<EditText>(R.id.input_link_txt).text.toString()
        var inReleaseDate = findViewById<EditText>(R.id.input_releaseDate_txt).text.toString()
        var inDiscoveryDate = findViewById<EditText>(R.id.input_discoveryDate_txt).text.toString()
        var inNotes = findViewById<EditText>(R.id.input_notes_txt).text.toString()

        thread {
            val music: MusicDao = AppDatabase.getInstance(applicationContext!!)?.musicDao()!!

            val musicID = getIntent().getExtras()!!.getInt("id")

            //update
            var release: Music = Music(music_id = musicID, name = inReleaseName, releaseType = inReleaseType, releaseDate = inReleaseDate, discoveryDate = inDiscoveryDate, link = inLink, favorite = false, rating = null, notes = inNotes, dataFromDiscogs = false)
            music.update(release)

            val artist: ArtistDao = AppDatabase.getInstance(applicationContext!!)?.artistDao()!!
            val genre: GenreDao = AppDatabase.getInstance(applicationContext!!)?.genreDao()!!

            //get artists
            var inArtists = findViewById<EditText>(R.id.input_artist_txt).text.toString()
            val listArtists = inArtists.lines()

            //insert artists
            for (i in listArtists)
            {
                var newArtist: Artist = Artist(i, null)
                artist.insertAll(newArtist)
            }

            //update artists crossrefs
            music.updateArtists(musicID, listArtists)

            //get genres
            var inGenres = findViewById<EditText>(R.id.input_genre_txt).text.toString()
            val listGenres = inGenres.lines()

            //insert genres
            for (i in listGenres)
            {
                var newGenre: Genre = Genre(i)
                genre.insertAll(newGenre)
            }

            //update genres
            music.updateGenres(musicID, listGenres)
        }

        finish()
    }


}