package com.quizlingo.verticalprototype

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.start_database_prototype_button).setOnClickListener {
            startActivity(Intent(this, DatabasePrototypeActivity::class.java))
        }
    }
}
