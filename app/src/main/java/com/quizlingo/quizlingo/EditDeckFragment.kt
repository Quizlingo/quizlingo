package com.quizlingo.quizlingo

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quizlingo.quizlingo.businesslogic.Card
import com.quizlingo.quizlingo.businesslogic.Deck
import kotlinx.coroutines.launch

class EditDeckFragment : Fragment(){

    private lateinit var viewAdapter: EditCardViewAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView

    data class CardItemModel(val type: ModelType, val card: Card?, val deck: Deck?) {
        enum class ModelType {
            DECK, CARD, ADD_BUTTON
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = LayoutInflater.from(container?.context)
            .inflate(R.layout.fragment_edit_deck, container, false)

        progressBar = view.findViewById(R.id.edit_list_loading_bar)
        recyclerView = view.findViewById(R.id.deck_edit_view)
        recyclerView.apply{
            layoutManager = LinearLayoutManager(activity)
            viewAdapter = EditCardViewAdapter()
            adapter = viewAdapter
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.e("Testing", "show loading bar")
        showLoadingBar()
        Log.e("Testing", "hide loading bar")
        hideLoadingBar()
    }


    open class CustomViewHolder(heldView: View) : RecyclerView.ViewHolder(heldView)

    class EditDeckViewHolder(view: ConstraintLayout) : CustomViewHolder(view) {
        val name: EditText = view.findViewById(R.id.edit_deck_name)
        val description: EditText = view.findViewById(R.id.edit_deck_description)
    }

    class EditCardViewHolder(view: ConstraintLayout) : CustomViewHolder(view) {
        val prompt: EditText = view.findViewById(R.id.edit_prompt)
        val text: EditText = view.findViewById(R.id.edit_text)

    }

    class AddItemViewHolder(view: ConstraintLayout) : CustomViewHolder(view) {
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
                listOf(addButton)
            }
            notifyDataSetChanged()
        }

        fun updateCards(cards: List<Card>) {
            this.cards = cards.map{ CardItemModel(CardItemModel.ModelType.CARD, it, null) }
            rebuildData()
        }

        fun updateDeck(deck: Deck) {
            this.deck = CardItemModel(CardItemModel.ModelType.DECK, null, deck)
            rebuildData()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            val inflater: LayoutInflater = LayoutInflater.from(parent.context)
            return when(CardItemModel.ModelType.values()[viewType]) {
                CardItemModel.ModelType.DECK -> {
                    val view = inflater.inflate(R.layout.deck_edit_item, parent,false) as ConstraintLayout
                    EditDeckViewHolder(view)
                }
                CardItemModel.ModelType.CARD -> {
                    val view = inflater.inflate(R.layout.card_edit_item, parent, false) as ConstraintLayout
                    EditCardViewHolder(view)
                }
                CardItemModel.ModelType.ADD_BUTTON -> {
                    val view = inflater.inflate(R.layout.add_list_item, parent ,false) as ConstraintLayout
                    AddItemViewHolder(view)
                }
            }
        }

        override fun onBindViewHolder(
            holder: CustomViewHolder,
            position: Int
        ) {
            val item = data[position]
        }

        /*
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
         */

        override fun getItemCount(): Int = data.size

        override fun getItemViewType(pos: Int) = data[pos].type.ordinal
    }

    private fun showLoadingBar() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun hideLoadingBar() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
}