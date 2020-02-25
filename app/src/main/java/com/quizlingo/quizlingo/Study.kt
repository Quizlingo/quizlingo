package com.quizlingo.quizlingo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import kotlinx.android.synthetic.main.activity_study.*
import java.security.Permission
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.github.zagum.speechrecognitionview.RecognitionProgressView
import com.github.zagum.speechrecognitionview.adapters.RecognitionListenerAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class Words() {
    val words = listOf<String>("butterfly", "tiger", "elephant", "monkey", "dog")
    var index = -1;

    fun next() : String? {
        index++;
        if(index >= words.size) {
            index = 0
        }
        return words[index]
    }

}

class Study : AppCompatActivity(), RecognitionListener {

    private val TAG = "SPEECH_REC"
    private val AUDIO_PERM_REQ = 1000
    private var words = Words()
    private lateinit var rpv : RecognitionProgressView
    private lateinit var speechRecognizer : SpeechRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        val button = findViewById<Button>(R.id.button)
        val text = findViewById<TextView>(R.id.match)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(this)
        rpv = findViewById<RecognitionProgressView>(R.id.recognition_view)
//        rpv.setSpeechRecognizer(speechRecognizer)

        text.setText(words.next())

        button.setOnClickListener {
            displaySpeechRecognizer()

        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            Log.e(TAG, "ADDING PERMISSION")
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                Log.e(TAG, "NO EXP NEEDED")
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    AUDIO_PERM_REQ)
            }
        } else {
            // Permission has already been granted
            Log.e(TAG, "permission already granted")
        }
//        rpv.setRecognitionListener(object : RecognitionListenerAdapter() {
//            override fun onResults(results: Bundle) {
//                showResults(results)
//            }
//        })

        var cols = listOf<Int>(Color.BLUE)
        rpv.setColors(cols.toIntArray())
        rpv.play()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        Log.e(TAG, "RECEIVED RESPONSE")
        when (requestCode) {
            AUDIO_PERM_REQ -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun displaySpeechRecognizer() {

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        }
//        if(Permission.)
        speechRecognizer.startListening(intent)
        Log.e(TAG, "starting up recognizer")
//        startActivityForResult(intent, SPEECH_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            val spokenText: String? =
//                if(data != null)
//                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).let { results ->
//                        results[0]
//                    }
//                else null
//            Log.e("DBUG", spokenText)
//            val word = findViewById<TextView>(R.id.match)
//            if(word.text == spokenText!!.toLowerCase()) {
//                Toast.makeText(this, "CORRECT!!", Toast.LENGTH_LONG)
//                word.setText(words.next())
//            }
//            else {
//                Toast.makeText(this, "Thats wrong, try again!", Toast.LENGTH_LONG)
//            }
//            // Do something with spokenText
//        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "onReadyForSpeech")
    }

    override fun onRmsChanged(rmsdB: Float) {
        Log.d(TAG, "onRmsChanged")
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        Log.d(TAG, "onBufferReceived")

    }

    override fun onPartialResults(partialResults: Bundle?) {
        Log.d(TAG, "onPartialResults")
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        Log.d(TAG, "onEvent $eventType")
    }

    override fun onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech")
        rpv.play();
    }

    override fun onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech")
//        rpv.stop();

    }

    override fun onError(error: Int) {
        Log.d(TAG, "onError $error")
    }

    override fun onResults(results: Bundle?) {
        Log.d(TAG, "onReadyForSpeech")
        val data = results?.getStringArrayList("results_recognition")

        val spokenText: String? = if (data != null) data[0] else ""
        Log.e("DBUG", spokenText)
        val word = findViewById<TextView>(R.id.match)
        if(word.text == spokenText!!.toLowerCase()) {
            Toast.makeText(this, "CORRECT!!", Toast.LENGTH_LONG).show()
            word.setText(words.next())
        }
        else {
            Toast.makeText(this, "Thats wrong, try again!", Toast.LENGTH_LONG).show()
        }
        displaySpeechRecognizer()
    }

}
