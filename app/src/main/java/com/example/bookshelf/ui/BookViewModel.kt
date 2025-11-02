package com.example.bookshelf.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.bookshelf.data.AppDatabase
import com.example.bookshelf.data.Book
import com.example.bookshelf.data.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = BookRepository(AppDatabase.get(app).bookDao())

    private val query = MutableLiveData("")
    private val readFilter = MutableLiveData<Boolean?>(null)

    val books: LiveData<List<Book>> = MediatorLiveData<List<Book>>().apply {
        val update: () -> Unit = {
            val q = query.value ?: ""
            val f = readFilter.value
            val live = if (q.isNotBlank()) repo.search(q)
            else repo.filterRead(f)
            addSource(live) { value = it }
        }
        addSource(query) { update() }
        addSource(readFilter) { update() }
        // initial
        update()
    }

    fun setQuery(text: String) { query.value = text }
    fun setFilter(read: Boolean?) { readFilter.value = read }

    fun addOrUpdate(id: Int?, title: String, author: String, rating: Int, read: Boolean, notes: String, onDone: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val book = if (id != null && id > 0) {
                val existing = repo.get(id) ?: Book(id=0, title=title, author=author, rating=rating, read=read, notes=notes)
                existing.copy(title = title, author = author, rating = rating, read = read, notes = notes)
            } else {
                Book(title = title, author = author, rating = rating, read = read, notes = notes)
            }
            val newId = if (book.id == 0) repo.add(book).toInt() else { repo.update(book); book.id }
            onDone(newId)
        }
    }

    fun load(id: Int, callback: (Book?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) { callback(repo.get(id)) }
    }

    fun deleteById(id: Int, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.get(id)?.let { repo.delete(it) }
            onDone()
        }
    }

    fun addOrUpdate(
        id: Int?,
        title: String,
        author: String,
        rating: Int,
        read: Boolean,
        notes: String,
        genre: String,                         // NEW
        onDone: (Int) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val book = if (id != null && id > 0) {
                val existing = repo.get(id) ?: Book(id=0, title=title, author=author, rating=rating, read=read, notes=notes, genre=genre)
                existing.copy(title = title, author = author, rating = rating, read = read, notes = notes, genre = genre)
            } else {
                Book(title = title, author = author, rating = rating, read = read, notes = notes, genre = genre)
            }
            val newId = if (book.id == 0) repo.add(book).toInt() else { repo.update(book); book.id }
            onDone(newId)
        }
    }

    fun setRead(id: Int, read: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val b = repo.get(id) ?: return@launch
            repo.update(b.copy(read = read))
        }
    }

}
