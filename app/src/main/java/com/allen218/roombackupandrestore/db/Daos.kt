/*
 * Copyright 2015-2020 .
 */

package com.allen218.roombackupandrestore.db

import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg user: User)

    // 更新
    @Update
    fun update(vararg user: User): Int

    // 删除
    @Delete
    fun delete(vararg user: User): Int
}
