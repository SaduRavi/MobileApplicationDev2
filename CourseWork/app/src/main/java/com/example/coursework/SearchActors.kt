package com.example.coursework

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.coursework.data.AppDatabase
import com.example.coursework.data.Movies
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class SearchActors : AppCompatActivity() {

    private lateinit var userInputActor: EditText
    private lateinit var display: TextView
    private lateinit var users: List<Movies>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_actors)
        userInputActor = findViewById(R.id.inputActorName)      //edit text to get the user input
        display = findViewById(R.id.textView3)                  //text view to display the movies

        if (savedInstanceState != null) {
            display.text    = savedInstanceState.getString("actors").toString()
        }

        val searchActorButton = findViewById<Button>(R.id.searchActorButton)
        searchActorButton.setOnClickListener {
            val userInput = userInputActor.text.toString()
            searchActor(userInput)
        }
    }

    /**
     * on save instance to save the key/necessary values when activity is destroyed
     */
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString("actors", display.text.toString())
    }

    /**
     * Method to search actors for the user given input
     */
    private fun searchActor(userInput: String) {
        val db = Room.databaseBuilder(this, AppDatabase::class.java, "movies_database").build()
        val movieDao = db.movieDao()

        runBlocking {
            launch {
                users = movieDao.getActorMovies(userInput)
                display.text = ""
                for (u in users) {
                    display.append("\n${u.title} -  ${u.year}")
                    val s = u.actors
                    display.append("\n${s}\n\n")
                }
            }
        }
    }
}