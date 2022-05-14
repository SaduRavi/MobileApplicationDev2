package com.example.coursework.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MoviesDao {
    @Query("Select * from movies")
    suspend fun getAll(): List<Movies>

    @Query("Select * from movies WHERE actors LIKE '%' || :actor ||'%'")
    suspend fun getActorMovies(actor: String): List<Movies>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(vararg movie: Movies)

    @Insert
    suspend fun insertAll(vararg movie: Movies)
}