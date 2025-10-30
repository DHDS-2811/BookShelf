package com.example.bookshelf.data

class BookRepository(private val dao: BookDao) {
    fun getAll() = dao.getAll()
    fun search(q: String) = if (q.isBlank()) dao.getAll() else dao.search(q)
    fun filterRead(read: Boolean?) = when (read) {
        true -> dao.filterByRead(true)
        false -> dao.filterByRead(false)
        null -> dao.getAll()
    }

    suspend fun get(id: Int) = dao.getById(id)
    suspend fun add(book: Book) = dao.insert(book)
    suspend fun update(book: Book) = dao.update(book)
    suspend fun delete(book: Book) = dao.delete(book)
}
