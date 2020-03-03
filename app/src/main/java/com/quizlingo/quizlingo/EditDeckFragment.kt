package com.quizlingo.quizlingo

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quizlingo.quizlingo.businesslogic.Card
import com.quizlingo.quizlingo.businesslogic.Deck
import com.quizlingo.quizlingo.businesslogic.MutableCard
import com.quizlingo.quizlingo.businesslogic.MutableDeck
import kotlinx.android.synthetic.main.home_deck_list_item.*
import java.lang.IllegalArgumentException

class EditDeckFragment : Fragment(){

    companion object {
        fun getInstance() = EditDeckFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var deck: MutableDeck

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_edit_deck, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        recyclerView = view.findViewById(R.id.deck_edit_view)

        deck = MutableDeck(viewModel.currentDeck.value ?: Deck(0L, "", "", listOf()))

        recyclerView.apply{
            layoutManager = LinearLayoutManager(activity)
            adapter = EditViewAdapter()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.currentDeck.value = deck.toDeck()
    }

    abstract inner class EditViewHolder(view: View): RecyclerView.ViewHolder(view)

    inner class EditDeckViewHolder(view: View) : EditViewHolder(view) {
        private val title: EditText = view.findViewById(R.id.edit_deck_title)
        private val description: EditText = view.findViewById(R.id.edit_deck_description)
        private lateinit var deck: MutableDeck

        fun setDeck(deck: MutableDeck) {
            this.deck = deck
            title.setText(deck.title)
            description.setText(deck.description)

            title.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    deck.title = s.toString()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

            })

            description.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    deck.description = s.toString()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

            })
        }

    }

    inner class EditCardViewHolder(view: View) : EditViewHolder(view) {
        private val prompt: EditText = view.findViewById(R.id.edit_card_prompt)
        private val text: EditText = view.findViewById(R.id.edit_card_text)
        private lateinit var card: MutableCard

        fun setCard(card: MutableCard) {
            this.card = card
            prompt.setText(card.prompt)
            text.setText(card.answer)

            prompt.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    card.prompt = s.toString()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

            })

            text.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    card.answer = s.toString()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

            })
        }
    }

    inner class AddCardViewHolder(view: View) : EditViewHolder(view) {
        val button: Button = view.findViewById(R.id.add_item_button)
    }

    inner class EditViewAdapter: RecyclerView.Adapter<EditViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditViewHolder {
            val inflater = LayoutInflater.from(requireActivity())
            return when(viewType) {
                R.layout.edit_deck_list_item -> EditDeckViewHolder(inflater.inflate(viewType, parent, false))
                R.layout.edit_card_list_item -> EditCardViewHolder(inflater.inflate(viewType, parent, false))
                R.layout.edit_add_list_item -> AddCardViewHolder(inflater.inflate(viewType, parent, false))
                else -> throw IllegalArgumentException("Invalid view type")
            }
        }

        override fun onBindViewHolder(holder: EditViewHolder, position: Int) {
            when(holder) {
                is EditDeckViewHolder -> {
                    holder.setDeck(deck)
                }
                is EditCardViewHolder -> {
                    holder.setCard(deck.cards[position-1])
                }
                is AddCardViewHolder -> {
                    holder.button.setOnClickListener{
                        deck.cards.add(MutableCard(Card(0L, deck.deckId, "", "")))
                        notifyItemInserted(deck.cards.size)
                    }
                }
            }
        }

        override fun getItemViewType(pos: Int): Int {
            return when(pos) {
                0 -> R.layout.edit_deck_list_item
                deck.cardCount + 1 -> R.layout.edit_add_list_item
                else -> R.layout.edit_card_list_item
            }
        }

        override fun getItemCount(): Int = deck.cardCount + 2

    }

}