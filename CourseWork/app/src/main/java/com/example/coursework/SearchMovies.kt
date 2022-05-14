package com.example.coursework

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.room.Room
import com.example.coursework.data.AppDatabase
import com.example.coursework.data.Movies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection


class SearchMovies : AppCompatActivity() {

    private lateinit var userInputMovie: EditText
    private lateinit var posterView: ImageView
    private lateinit var displayMovie: TextView
    private lateinit var errorText: TextView
    private lateinit var saveMoviesButton: Button
    private lateinit var retrieveMoviesButton: Button

    private lateinit var id      : String
    private lateinit var title   : String
    private lateinit var year    : String
    private lateinit var rated   : String
    private lateinit var released: String
    private lateinit var runtime : String
    private lateinit var genre   : String
    private lateinit var director: String
    private lateinit var writer  : String
    private lateinit var actors  : String
    private lateinit var plot    : String
    private lateinit var posterUrl: Any

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_movies)

        val db = Room.databaseBuilder(this, AppDatabase::class.java, "movies_database").build()
        val movieDao = db.movieDao()

        userInputMovie   = findViewById(R.id.inputMovieName)        // edit text to get the user input
        posterView       = findViewById(R.id.posterView)            // image view to display the poster
        displayMovie     = findViewById(R.id.displayMovieDetails)   // text view to display the movies
        errorText        = findViewById(R.id.errorText)             // display the error if the movie does not exist
        saveMoviesButton = findViewById(R.id.saveMovie)             // button to save the movie to the database
        retrieveMoviesButton = findViewById(R.id.retriveMovie)      // button to extract the movie from the web

        if (savedInstanceState != null) {
            title    = savedInstanceState.getString("title").toString()
            year     = savedInstanceState.getString("year").toString()
            rated    = savedInstanceState.getString("rated").toString()
            released = savedInstanceState.getString("released").toString()
            runtime  = savedInstanceState.getString("runtime").toString()
            genre    = savedInstanceState.getString("genre").toString()
            director = savedInstanceState.getString("director").toString()
            writer   = savedInstanceState.getString("writer").toString()
            actors   = savedInstanceState.getString("actors").toString()
            plot     = savedInstanceState.getString("plot").toString()
            posterUrl= savedInstanceState.getString("posterURL").toString()
            displayMovieDetails()
        }

        retrieveMoviesButton.setOnClickListener {
            val userInputMovie = userInputMovie.text
            getMovieFromWeb(userInputMovie)
            this.userInputMovie.setText("")
            displayMovieDetails()
        }
        //button to add the retrieved movie to the database
        saveMoviesButton.setOnClickListener {
            runBlocking {
                launch {
                    val user = Movies(id,title,year, rated, released, runtime, genre, director, writer, actors, plot)
                    movieDao.insertUsers(user)
                    Toast.makeText(this@SearchMovies, "Successfully Added The Movies to the Database!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    /**
     * Method to display the entered movie details
     */
    @SuppressLint("SetTextI18n")
    private fun displayMovieDetails(){
        if(title != ""){
            if(posterUrl == "N/A"){
                posterView.setImageResource(R.drawable.x)
            }
            else{
                displayPoster()
            }
            saveMoviesButton.isEnabled = true  //enabling the save movie button
            errorText.text = ""                //resetting the error text to empty
            displayMovie.text = "\n\n\n\n" +   //adding the movie information to the displaying text
                    "Title    : $title\n\n\n" +
                    "Year     : $year\n\n" +
                    "Rated    : $rated\n\n" +
                    "Released : $released\n\n" +
                    "Runtime  : $runtime\n\n" +
                    "Genre    : $genre\n\n" +
                    "Director : $director\n" +
                    "Writer   : $writer\n" +
                    "Actors   : $actors\n\n" +
                    "Plot     : $plot"
        }
        if(title == ""){    //if the title is empty entered movie is invalid
            saveMoviesButton.isEnabled = false       //disabling the save movie button
            errorText.text = "INVALID MOVIE NAME"    //displaying text invalid movie
            displayMovie.text = ""                   //resetting the movie information text to empty
            posterView.isVisible = false             //hiding the poster
        }
    }
    /**
     * on save instance to save the key/necessary values when activity is destroyed
     */
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString("title", title)
        savedInstanceState.putString("year", year)
        savedInstanceState.putString("rated", rated)
        savedInstanceState.putString("released", released)
        savedInstanceState.putString("runtime", runtime)
        savedInstanceState.putString("genre", genre)
        savedInstanceState.putString("director", director)
        savedInstanceState.putString("writer", writer)
        savedInstanceState.putString("actors", actors)
        savedInstanceState.putString("plot", plot)
        savedInstanceState.putString("posterURL", posterUrl.toString())
    }
    /**
     * Method to the get the Json object of the movie name enter by the user
     */
    private fun getMovieFromWeb(userInputMovie: Editable) {
        var movieName = userInputMovie.toString()
        movieName = movieName.replace("\\s".toRegex(), "+")

        val stb = StringBuilder()
        val urlString = "https://www.omdbapi.com/?t=$movieName&apikey=74742c91"     //api key
        val url = URL(urlString)
        val con: HttpsURLConnection = url.openConnection() as HttpsURLConnection

        runBlocking {
            launch {
                withContext(Dispatchers.IO) {
                    val bf = BufferedReader(InputStreamReader(con.inputStream))
                    var line: String? = bf.readLine()
                    while (line != null) {
                        stb.append(line + "\n")
                        line = bf.readLine()
                    }
                    parseJSON(stb)
                }
            }
        }
    }
    /**
     * Method to extract necessary information from the received json object
     */
    private fun parseJSON(stb: java.lang.StringBuilder) {
        val json = JSONObject(stb.toString())
        val validMovies = json["Response"]
        if(validMovies == "True"){ //if the enter movie is valid information is extracted and set to its respective variables
            posterUrl = json["Poster"]
            id       = json["imdbID"].toString()
            title    = json["Title"].toString()
            year     = json["Year"].toString()
            rated    = json["Rated"].toString()
            released = json["Released"].toString()
            runtime  = json["Runtime"].toString()
            genre    = json["Genre"].toString()
            director = json["Director"].toString()
            writer   = json["Writer"].toString()
            actors   = json["Actors"].toString()
            plot     = json["Plot"].toString()
        }
        else{   //if the movie entered is invalid the title is set to empty
            title    = ""
        }

    }
    /**
     * Method to display the poster of the movie
     */
    private fun displayPoster() {     //https://www.geeksforgeeks.org/how-to-load-any-image-from-url-without-using-any-dependency-in-android/
        val imageURL = posterUrl.toString()
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        var image: Bitmap?
        executor.execute {
            try {
                val ins = URL(imageURL).openStream()
                image = BitmapFactory.decodeStream(ins)
                handler.post {
                    posterView.setImageBitmap(image)
                    posterView.isVisible = true
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}