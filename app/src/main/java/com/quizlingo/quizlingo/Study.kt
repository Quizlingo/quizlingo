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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.zagum.speechrecognitionview.RecognitionProgressView
import com.quizlingo.quizlingo.businesslogic.*

class Study : Fragment(), RecognitionListener {

    private val TAG = "SPEECH_REC"
    private val AUDIO_PERM_REQ = 1000
    private lateinit var vm : MainViewModel
    private lateinit var deck : Deck
    private lateinit var cards : List<Card>
    private lateinit var rpv : RecognitionProgressView
    private lateinit var speechRecognizer : SpeechRecognizer
    private lateinit var card : TextView
    private var current = 0;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.activity_study, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        vm = ViewModelProvider(this).get(MainViewModel::class.java)
        var mld : MutableLiveData<Deck> = vm.currentDeck
//        var deckk = Deck(0, "Animals", "tbd",
//            cards = listOf(Card(1, 1, "small animal", "cat")), cardCount = 1)
//        mld.postValue(deckk)

        val restart = view.findViewById<Button>(R.id.restart)
        var title = view.findViewById<TextView>(R.id.title)
        card = view.findViewById<TextView>(R.id.match)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer.setRecognitionListener(this)
        rpv = view.findViewById<RecognitionProgressView>(R.id.recognition_view)
//        rpv.setSpeechRecognizer(speechRecognizer)

        title.setText("Waiting for deck to load...")
        mld?.observe(viewLifecycleOwner, Observer {d ->
            deck = d
            title.setText(d.title)
            cards = d.cards
            startFlashcards()
        })

        if (ContextCompat.checkSelfPermission( context!!,
                Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    AUDIO_PERM_REQ)
            }
        } else {
            // Permission has already been granted
        }
//        rpv.setRecognitionListener(object : RecognitionListenerAdapter() {
//            override fun onResults(results: Bundle) {
//                showResults(results)
//            }
//        })

        restart.setOnClickListener {
            speechRecognizer.stopListening()
            displaySpeechRecognizer()
        }

        var cols = listOf<Int>(Color.BLUE)
        rpv.setColors(cols.toIntArray())
        rpv.play()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
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
//        startActivityForResult(intent, SPEECH_REQUEST_CODE)
    }

    private fun startFlashcards() {
        if(deck.cardCount == current) {
            finishFlashcards()
        }
        else {
            card.setText(cards.get(current).prompt)
            displaySpeechRecognizer()
        }
    }

    private fun finishFlashcards() {
        card.setText("")
        speechRecognizer.stopListening()
        Toast.makeText(context, "You finished the deck!", Toast.LENGTH_LONG)
    }

    override fun onReadyForSpeech(params: Bundle?) {
//        Log.d(TAG, "onReadyForSpeech")
    }

    override fun onRmsChanged(rmsdB: Float) {
//        Log.d(TAG, "onRmsChanged")
    }

    override fun onBufferReceived(buffer: ByteArray?) {
//        Log.d(TAG, "onBufferReceived")

    }

    override fun onPartialResults(partialResults: Bundle?) {
//        Log.d(TAG, "onPartialResults")
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
//        Log.d(TAG, "onEvent $eventType")
    }

    override fun onBeginningOfSpeech() {
//        Log.d(TAG, "onBeginningOfSpeech")
//        rpv.play();
    }

    override fun onEndOfSpeech() {
//        Log.d(TAG, "onEndOfSpeech")
//        rpv.stop();

    }

    override fun onError(error: Int) {
//        Log.d(TAG, "onError $error")
    }

    override fun onResults(results: Bundle?) {
//        Log.d(TAG, "onReadyForSpeech")
        val data = results?.getStringArrayList("results_recognition")

        val spokenText: String? = if (data != null) data[0] else ""
        var ans = cards.get(current).answer
        if(ans == spokenText!!.toLowerCase()) {
            Toast.makeText(context, "CORRECT!!", Toast.LENGTH_LONG).show()
            current++
            startFlashcards()
        }
        else {
            Toast.makeText(context, "Thats wrong, try again!", Toast.LENGTH_LONG).show()
        }
    }
}