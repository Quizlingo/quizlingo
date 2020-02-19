package com.quizlingo.verticalprototype

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class Words() {
    val words = listOf<String>("butterfly", "i am awesome", "does this work?")
    var index = -1;

    fun next() : String? {
        index++;
        if(index > words.size) {
            return null
        }
        return words[index]
    }

}

class MainActivity : AppCompatActivity() {

    private val SPEECH_REQUEST_CODE = 0;
    private var words = Words();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button).setOnClickListener {
            displaySpeechRecognizer()
        }

        findViewById<TextView>(R.id.match).setText(words.next());
    }

    private fun displaySpeechRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        }
        startActivityForResult(intent, SPEECH_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                if(data != null)
                data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).let { results ->
                    results[0]
                }
                else null
            Log.e("DBUG", spokenText)
            val word = findViewById<TextView>(R.id.match)
            if(word.text == spokenText!!.toLowerCase()) {
                Toast.makeText(this, "CORRECT!!", Toast.LENGTH_LONG)
                word.setText(words.next())
            }
            else {
                Toast.makeText(this, "Thats wrong, try again!", Toast.LENGTH_LONG)
            }
            // Do something with spokenText
        }
//        super.onActivityResult(requestCode, resultCode, data)
    }
}
