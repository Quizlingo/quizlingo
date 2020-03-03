package com.quizlingo.verticalprototype

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreSetup : AppCompatActivity() {

    private lateinit var input: EditText
    private lateinit var list: ViewGroup

    private val FIRST_NAME = "first"
    private val LAST_NAME = "last"
    private val TAG = "TESTING"

    private lateinit var nameTextView: TextView

    private var myDocRef = FirebaseFirestore.getInstance().document("users/names")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firestore_setup)

        nameTextView = findViewById(R.id.name)

        findViewById<Button>(R.id.save).setOnClickListener{ saveName() }
        findViewById<Button>(R.id.fetch).setOnClickListener{ fetchName()}


    }

    private fun saveName(){
        var firstView = findViewById<EditText>(R.id.first)
        var lastView = findViewById<EditText>(R.id.last)

        var firstText = firstView.text.toString()
        var lastText = lastView.text.toString()

        if (firstText.isEmpty() || lastText.isEmpty()){
            return
        }

        var dataToSave = hashMapOf<String, Any>()
        dataToSave.put(FIRST_NAME, firstText)
        dataToSave.put(LAST_NAME, lastText)

        myDocRef.set(dataToSave)
            .addOnSuccessListener { result ->
                Log.d(TAG, "Data has been added")
            }
            .addOnFailureListener{ result ->
                Log.d(TAG, "Data failed to be added!")
            }
    }

    private fun fetchName(){
        myDocRef.get()
            .addOnSuccessListener { result ->
                if (result.exists()){
                    val firstText = result.getString(FIRST_NAME)
                    val lastText = result.getString(LAST_NAME)

                    nameTextView.setText(firstText + " " + lastText)
                }
            }
            .addOnFailureListener{ result->
                Log.d(TAG, "Failed to fetch data")
            }
    }
}
