package com.example.coursework

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.room.Room
import com.example.coursework.data.AppDatabase
import com.example.coursework.data.Movies
import com.example.coursework.data.MoviesDao
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {

    private lateinit var startupView    : ConstraintLayout
    private lateinit var searchView     : ConstraintLayout
    private lateinit var displayMovies  : TextView
    private lateinit var userInputMovie : EditText
    private lateinit var movies : java.lang.StringBuilder


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startupView   = findViewById(R.id.startupView)      //startup layout
        searchView    = findViewById(R.id.searchView)       //search layout
        displayMovies = findViewById(R.id.displayMoviesFromWeb) //text view to display the extracted movies
        userInputMovie= findViewById(R.id.userInputMovie)       //edit text to get the user input
        movies = java.lang.StringBuilder()                      //string builder to build a string from all the extracted movies

        if (savedInstanceState != null) {
            displayMovies.text    = savedInstanceState.getString("movies").toString()
        }

        val db = Room.databaseBuilder(this, AppDatabase::class.java, "movies_database").build() //building the database
        val movieDao = db.movieDao()

        val searchMovieWeb = findViewById<FloatingActionButton>(R.id.searchFromWebsiteButton)  //button to switch to layout to view movies from the web
        searchMovieWeb.setOnClickListener {
            startupView.isVisible = false   //hiding the start up layout
            searchView.isVisible = true     //viewing the search layout
        }

        val back = findViewById<FloatingActionButton>(R.id.backToStartup)
        back.setOnClickListener {
            startupView.isVisible = true    //viewing the startup layout
            searchView.isVisible = false    //hiding the search layout
        }


        val addLocalMoviesButton = findViewById<TextView>(R.id.addLocalMovies)  //button to add the movies to database
        addLocalMoviesButton.setOnClickListener {
            addLocalMoviesToDatabase(movieDao)
        }

        val searchMoviesButton = findViewById<Button>(R.id.searchMovies)        //button to open a new activity for searching movies
        searchMoviesButton.setOnClickListener {
            val contactIntent = Intent(this, SearchMovies::class.java)
            startActivity(contactIntent)
        }

        val searchActorButton = findViewById<Button>(R.id.searchActor)          //button the open a new activity for searching actors
        searchActorButton.setOnClickListener {
            val contactIntent = Intent(this, SearchActors::class.java)
            startActivity(contactIntent)
        }

        userInputMovie.addTextChangedListener(object : TextWatcher {             //https://www.tutorialkart.com/kotlin-android/android-edittext-on-text-change/
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                getMoviesFromWeb()
                displayMovies.text = movies
            }
        })
    }
    /**
     * on save instance to save the key/necessary values when activity is destroyed
     */
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString("movies", displayMovies.text.toString())
    }
    /**
     * Method to the get the Json object of the movie name enter by the user
     */
    private fun getMoviesFromWeb() {
        var movieName = userInputMovie.text.toString()
        movieName = movieName.replace("\\s".toRegex(), "+")

        val stb = StringBuilder()
        val urlString = "https://www.omdbapi.com/?apikey=74742c91&s=*$movieName*"      //api key
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
        if (json["Response"] != "False"){
            movies = java.lang.StringBuilder()
            val jsonArray: JSONArray = json.getJSONArray("Search")
            for (i in 0 until jsonArray.length()) {
                val movieArray: JSONObject = jsonArray[i] as JSONObject
                val title = movieArray["Title"] as String
                val year = movieArray["Year"] as String
                movies.append("${i+1}) $title - $year \n\n")
            }
        }
    }
    /**
     * Method to add the hard coded movies to the database
     */
    private fun addLocalMoviesToDatabase(movieDao: MoviesDao) {
        val moviesArray = arrayOf(
            arrayOf(
                "tt0111161",
                "The Shawshank Redemption",
                "1994",
                "R",
                "14 Oct 1994",
                "142 min",
                "Drama",
                "Frank Darabont",
                "Stephen King, Frank Darabont",
                "Tim Robbins, Morgan Freeman, Bob Gunton",
                "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency."
            ),
            arrayOf(
                "tt2313197",
                "Batman: The Dark Knight Returns, Part 1",
                "2012",
                "PG-13",
                "25 Sep 2012",
                "76 min",
                "Animation, Action, Crime, Drama, Thriller",
                "Jay Oliva",
                "Bob Kane (character created by: Batman), Frank Miller (comic book), Klaus Janson (comic book), Bob Goodman",
                "Peter Weller, Ariel Winter, David Selby, Wade Williams",
                "Batman has not been seen for ten years. A new breed of criminal ravages Gotham City, forcing 55-year-old Bruce Wayne back into the cape and cowl. But, does he still have what it takes to fight crime in a new era?"
            ),
            arrayOf(
                "tt0167260",
                "The Lord of the Rings: The Return of the King",
                "2003",
                "PG-13",
                "17 Dec 2003",
                "201 min",
                "Action, Adventure, Drama",
                "Peter Jackson",
                "J.R.R. Tolkien, Fran Walsh, Philippa Boyens",
                "Elijah Wood, Viggo Mortensen, Ian McKellen",
                "Gandalf and Aragorn lead the World of Men against Sauron's army to draw his gaze from Frodo and Sam as they approach Mount Doom with the One Ring."

            ),
            arrayOf(
                "tt1375666",
                "Inception",
                "2010",
                "PG-13",
                "16 Jul 2010",
                "148 min",
                "Action, Adventure, Sci-Fi",
                "Christopher Nolan",
                "Christopher Nolan",
                "Leonardo DiCaprio, Joseph Gordon-Levitt, Elliot Page",
                "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O., but his tragic past may doom the project and his team to disaster.",
            ),
            arrayOf(
                "tt0133093",
                "The Matrix",
                "1999",
                "R",
                "31 Mar 1999",
                "136 min",
                "Action, Sci-Fi",
                "Lana Wachowski, Lilly Wachowski",
                "Lilly Wachowski, Lana Wachowski",
                "Keanu Reeves, Laurence Fishburne, Carrie-Anne Moss",
                "When a beautiful stranger leads computer hacker Neo to a forbidding underworld, he discovers the shocking truth--the life he knows is the elaborate deception of an evil cyber-intelligence."
            ),
        ) //array of all the movies
        runBlocking {
            launch {
                for (i in 0..4) {
                    val id       = moviesArray[i][0]
                    val title    = moviesArray[i][1]
                    val year     = moviesArray[i][2]
                    val rated    = moviesArray[i][3]
                    val released = moviesArray[i][4]
                    val runtime  = moviesArray[i][5]
                    val genre    = moviesArray[i][6]
                    val director = moviesArray[i][7]
                    val writer   = moviesArray[i][8]
                    val actors   = moviesArray[i][9]
                    val plot     = moviesArray[i][10]

                    val user = Movies(id,title,year, rated, released, runtime, genre, director, writer, actors, plot) //creating and adding the movie to the database
                    movieDao.insertUsers(user)
                }
                Toast.makeText(this@MainActivity, "Successfully Added The Movies to the Database!", Toast.LENGTH_SHORT).show()
            }
        }
    }

}