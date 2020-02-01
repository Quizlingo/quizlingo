package com.quizlingo.verticalprototype

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EditDeckActivity : AppCompatActivity() {

    data class CardItemModel(val type: ModelType, val card: DatabaseComponent.Card?, val deck: DatabaseComponent.Deck?) {
        enum class ModelType {
            DECK, CARD, ADD_BUTTON
        }
    }

    open class CustomViewHolder(heldView: View) : RecyclerView.ViewHolder(heldView)

    class EditDeckViewHolder(heldView: View) : CustomViewHolder(heldView) {
        private val view: ConstraintLayout = heldView as ConstraintLayout
        val name: EditText = view.findViewById(R.id.edit_deck_name)
        val description: EditText = view.findViewById(R.id.edit_deck_description)
    }

    class EditCardViewHolder(heldView: View) : CustomViewHolder(heldView) {
        private val view: ConstraintLayout = heldView as ConstraintLayout
        val prompt: EditText = view.findViewById(R.id.edit_prompt)
        val text: EditText = view.findViewById(R.id.edit_text)

    }

    class AddItemViewHolder(heldView: View) : CustomViewHolder(heldView) {
        private val view: ConstraintLayout = heldView as ConstraintLayout
        val button: Button = view.findViewById(R.id.add_item_button)
    }

    class EditCardViewAdapter: RecyclerView.Adapter<CustomViewHolder>() {

        private val addButton = CardItemModel(CardItemModel.ModelType.ADD_BUTTON, null, null)
        private var deck : CardItemModel? = null
        private var cards : List<CardItemModel>? = null
        private var data: List<CardItemModel> = listOf(addButton)

        var cardCreator: View.OnClickListener? = null

        private fun rebuildData() {
            data = if(deck != null && cards != null) {
                listOf(deck!!) + cards!! + addButton
            } else {
                // FIXME: Some sort of loading icon should be displayed until the list is loaded, not the add button
                listOf(addButton)
            }
            notifyDataSetChanged()
        }

        fun updateCards(cards: List<DatabaseComponent.Card>) {
            this.cards = cards.map{ CardItemModel(CardItemModel.ModelType.CARD, it, null) }
            rebuildData()
        }

        fun updateDeck(deck: DatabaseComponent.Deck) {
            this.deck = CardItemModel(CardItemModel.ModelType.DECK, null, deck)
            rebuildData()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            val inflater: LayoutInflater = LayoutInflater.from(parent.context)
            return when(CardItemModel.ModelType.values()[viewType]) {
                CardItemModel.ModelType.DECK -> {
                    val view = inflater.inflate(R.layout.deck_edit_item, parent,false)
                    EditDeckViewHolder(view)
                }
                CardItemModel.ModelType.CARD -> {
                    val view = inflater.inflate(R.layout.card_edit_item, parent, false)
                    EditCardViewHolder(view)
                }
                CardItemModel.ModelType.ADD_BUTTON -> {
                    val view = inflater.inflate(R.layout.add_list_item, parent ,false)
                    AddItemViewHolder(view)
                }
            }
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            val item = data[position]

            when(item.type) {
                CardItemModel.ModelType.DECK -> {
                    val holder = holder as EditDeckViewHolder
                    val deck = item.deck!!
                    holder.name.setText(deck.deckName)
                    holder.description.setText(deck.deckDescription)

                    holder.name.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            deck.deckName = s.toString()
                        }
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        }
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        }
                    })
                    holder.description.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            deck.deckDescription = s.toString()
                        }
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        }
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        }
                    })
                }
                CardItemModel.ModelType.CARD -> {
                    val holder = holder as EditCardViewHolder
                    val card = item.card!!
                    holder.prompt.setText(card.cardPrompt)
                    holder.text.setText(card.cardText)

                    holder.prompt.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            card.cardPrompt = s.toString()
                        }
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        }
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        }
                    })

                    holder.text.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            card.cardText = s.toString()
                        }
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        }
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        }
                    })
                }
                CardItemModel.ModelType.ADD_BUTTON -> {
                    val holder = holder as AddItemViewHolder
                    if(cardCreator != null)
                        holder.button.setOnClickListener(cardCreator)
                }
            }
        }

        override fun getItemCount(): Int = data.size

        override fun getItemViewType(pos: Int) = data[pos].type.ordinal
    }

    class EditDeckViewModel : ViewModel() {
        var cards: MutableLiveData<List<DatabaseComponent.Card>> = MutableLiveData()
        var deck: MutableLiveData<DatabaseComponent.Deck> = MutableLiveData()

        fun newDeck() {
            cards.value = mutableListOf()
            deck.value = DatabaseComponent.Deck(0L, "", "", 0)
        }

    }

    companion object Constants {
        const val editModeKey: String = "com.quizlingo.DECK_EDIT_MODE"
        const val editDeckIdKey: String = "com.quizlingo.DECK_EDIT_ID"
    }
    enum class EditModes {NEW, EDIT}

    private lateinit var viewAdapter: EditCardViewAdapter
    private lateinit var viewModel: EditDeckViewModel
    private lateinit var database: DatabaseComponent.AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_deck)

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return modelClass.getConstructor().newInstance()
            }
        }).get(EditDeckViewModel::class.java)

        database = DatabaseComponent.getDatabase(this)

        if(viewModel.deck.value == null) {

            if(intent.getSerializableExtra(editModeKey) == EditModes.EDIT) {
                val def = -1L
                val deckId = intent.getLongExtra(editDeckIdKey, def)
                if(deckId == def) {
                    // I'm not sure that deck ids are guaranteed to be positive, so I don't want to throw an error here
                    // However, it is mighty suspicious if the deck id matches the default value, so print a warning
                    Log.w("EditDeckActivity", "Deck ID matches default value")
                }

                database.cardDao().getCardsByDeck(deckId).observe(this, Observer {
                    viewModel.cards.value = it
                })
                database.deckDao().getDeckById(deckId).observe(this, Observer {
                    viewModel.deck.value = it
                })
            } else {
               viewModel.newDeck()
            }
        }


        val recyclerView: RecyclerView = findViewById(R.id.deck_edit_view)
        viewAdapter = EditCardViewAdapter()
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = viewAdapter

        viewAdapter.cardCreator = View.OnClickListener {
            if(viewModel.deck.value == null) {
                Log.w("EditDeckActivity", "Refusing to add card to null deck")
            } else {
                viewModel.cards.value = viewModel.cards.value!! + DatabaseComponent.Card(0L, viewModel.deck.value!!.id, "", "")
            }
        }

        viewModel.cards.observe(this, Observer {
            viewAdapter.updateCards(it)
        })

        viewModel.deck.observe(this, Observer {
            viewAdapter.updateDeck(it)
        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveButton -> {
                saveCards()
                finish()
                true
            }
            else  -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveCards() {
        viewModel.deck.value!!.deckCardCount = viewModel.cards.value!!.size
        if (viewModel.deck.value == null || viewModel.cards.value == null) {
            Log.w("EditDeckActivity", "Cannot save uninitialized deck")
        }else if(viewModel.deck.value!!.id == 0L) {
            SaveDeckTask(database.deckDao()) {deckId: Long ->
                viewModel.cards.value!!.forEach{it.deckId = deckId}
                SaveCardsTask(database.cardDao()).execute(*(viewModel.cards.value!!.toTypedArray()))
            }.execute(viewModel.deck.value!!)
        } else {
            SaveDeckTask(database.deckDao(), null).execute(viewModel.deck.value!!)
            viewModel.cards.value!!.forEach{it.deckId = viewModel.deck.value!!.id}
            SaveCardsTask(database.cardDao()).execute(*viewModel.cards.value!!.toTypedArray())
        }
    }
}
