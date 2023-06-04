package database

import kotlinx.coroutines.Deferred

interface Repository {
    suspend fun createBook(title: String, isbn: String): Deferred<Long>
}