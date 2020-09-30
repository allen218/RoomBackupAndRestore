package com.allen218.roombackupandrestore

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.allen218.roombackupandrestore.db.DbManager
import com.allen218.roombackupandrestore.db.User

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        operateDb()
    }

    private fun operateDb() {
        DbManager.userDao.insertAll(User(0, "allen", "123456", 30))
        DbManager.userDao.insertAll(User(1, "allen", "123456", 30))
        DbManager.userDao.update(User(0, "allen", "456", 30))
        DbManager.userDao.delete(User(1, "allen", "123456", 30))
    }
}