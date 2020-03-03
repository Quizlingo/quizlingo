package com.quizlingo.quizlingo

import android.Manifest
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.zagum.speechrecognitionview.RecognitionProgressView
import com.quizlingo.quizlingo.businesslogic.*

class StudyFragment : Fragment(), RecognitionListener {

    companion object {
        fun getInstance() = StudyFragment()

        private const val TAG = "com.quizlingo.quizlingo.Study.speech_rec"
        private const val AUDIO_PERM_REQ = 1000
    }

    private lateinit var vm: MainViewModel
    private lateinit var deck: Deck
    private lateinit var cards: List<Card>
    private lateinit var recognitionProgressView: RecognitionProgressView
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var card: TextView
    private var current = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_study, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity()

        vm = ViewModelProvider(activity).get(MainViewModel::class.java)
        val currentDeckLiveData: MutableLiveData<Deck> = vm.currentDeck

        val restart = view.findViewById<Button>(R.id.restart)
        val title = view.findViewById<TextView>(R.id.title)

        card = view.findViewById(R.id.match)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity)
        recognitionProgressView = view.findViewById(R.id.recognition_view)

        recognitionProgressView.setSpeechRecognizer(speechRecognizer)
        recognitionProgressView.setRecognitionListener(this)


        // TODO: Extract string resource
        title.text = "Waiting for deck to load..."

        currentDeckLiveData.observe(viewLifecycleOwner, Observer { newDeck ->
            deck = newDeck
            title.text = newDeck.title
            cards = newDeck.cards
            startFlashcards()
        })

        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                // TODO: Show an explanation
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    AUDIO_PERM_REQ
                )
            }
        }

        restart.setOnClickListener {

            startSpeechRecognition()
        }

        var recognitionColors = listOf(Color.BLUE)
        recognitionProgressView.setColors(recognitionColors.toIntArray())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            AUDIO_PERM_REQ -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // TODO
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

    private fun startSpeechRecognition() {
        speechRecognizer.stopListening()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
        }
        recognitionProgressView.play()
        speechRecognizer.startListening(intent)
    }

    private fun startFlashcards() {
        if (deck.cardCount == current) {
            finishFlashcards()
        } else {
            card.text = cards[current].prompt
            startSpeechRecognition()
        }
    }

    private fun finishFlashcards() {
        card.text = ""
        speechRecognizer.stopListening()
        Toast.makeText(context, "You finished the deck!", Toast.LENGTH_LONG).show()
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

        val spokenText: String? = data?.get(0) ?: ""
        val ans = cards[current].answer
        if (ans == spokenText!!.toLowerCase()) {
            Toast.makeText(context, "CORRECT!!", Toast.LENGTH_LONG).show()
            current++
            startFlashcards()
        } else {
            Toast.makeText(context, "Thats wrong, try again!", Toast.LENGTH_LONG).show()
        }
    }
}