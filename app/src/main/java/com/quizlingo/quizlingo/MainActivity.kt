package com.quizlingo.quizlingo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.content, Study())
        fragmentTransaction.commit()


         */
        if (savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .add(R.id.content, EditDeckFragment())
                .commit()
        }
    }
}
