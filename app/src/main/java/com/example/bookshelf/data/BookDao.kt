package com.example.bookshelf.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY title ASC")
    fun getAll(): LiveData<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getById(id: Int): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: Book): Long

    @Update suspend fun update(book: Book)

    @Delete suspend fun delete(book: Book)

    @Query("SELECT * FROM books WHERE title LIKE '%' || :q || '%' OR author LIKE '%' || :q || '%' ORDER BY title ASC")
    fun search(q: String): LiveData<List<Book>>

    @Query("SELECT * FROM books WHERE read = :isRead ORDER BY title ASC")
    fun filterByRead(isRead: Boolean): LiveData<List<Book>>
}
