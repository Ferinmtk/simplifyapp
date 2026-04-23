//save user when they log in and find them when app launches
package com.simplifybiz.mobile.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // Save user on login (Overwrite if they exist)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Get the current user (There should usually only be one)
    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    // Get user synchronously (for SessionManager checks)
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUserSync(): UserEntity?

    // Logout
    @Query("DELETE FROM users")
    suspend fun clearUser()

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUser(): UserEntity?
}
