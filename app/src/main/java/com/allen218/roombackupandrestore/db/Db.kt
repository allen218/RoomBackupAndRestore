/*
 * Copyright 2015-2020 .
 */

package com.allen218.roombackupandrestore.db

import androidx.room.Room
import androidx.room.RoomDatabase
import com.allen218.roombackupandrestore.db.function.Backup
import com.allen218.roombackupandrestore.db.function.Restore
import com.allen218.roombackupandrestore.appCtx

abstract class Db : RoomDatabase() {
    abstract fun userDao(): UserDao
}

@Suppress("MemberVisibilityCanBePrivate")
object DbManager {
    var mock: Boolean = false
    private val mockDb: Db by lazy {
        Room
            .inMemoryDatabaseBuilder(
                appCtx,
                Db::class.java
            )
            .allowMainThreadQueries()
            // .addMigrations(*ALL_MIGRATIONS)
            .build()
    }

    private val actualDb =
        Room.databaseBuilder(
            appCtx,
            Db::class.java,
            "DB_TEST"
        )
            // .addMigrations(*ALL_MIGRATIONS)
            .allowMainThreadQueries()
            .build()

    val db: Db
        get() {
            return if (mock) {
                mockDb
            } else {
                actualDb
            }
        }

    val userDao: UserDao
        get() = db.userDao()

    fun backup() {
        Backup.Init().database(db)
            .execute()
    }

    fun restore() {
        Restore.Init().database(db)
            .execute()
    }
}
