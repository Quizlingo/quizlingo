package com.quizlingo.quizlingo

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quizlingo.quizlingo.businesslogic.Card
import com.quizlingo.quizlingo.businesslogic.Deck
import com.quizlingo.quizlingo.businesslogic.MutableCard
import com.quizlingo.quizlingo.businesslogic.MutableDeck
import java.lang.IllegalArgumentException

class EditDeckFragment : Fragment() {

    companion object {
        fun getInstance() = EditDeckFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var deck: MutableDeck
    private lateinit var itemTouchHelper: ItemTouchHelper

    private lateinit var editViewAdapter: EditViewAdapter

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

        editViewAdapter = EditViewAdapter()

        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = editViewAdapter
        }

        itemTouchHelper = ItemTouchHelper(EditorTouchHelperCallback())
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        deck.cards.forEachIndexed{idx, card -> card.order = idx}
        viewModel.currentDeck.value = deck.toDeck()
    }


    inner class EditorTouchHelperCallback : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.RIGHT
    ) {
        private val deleteIcon = requireActivity().getDrawable(R.drawable.ic_delete_white_24dp)
        private val deleteBackground = ColorDrawable(Color.RED)

        override fun getSwipeDirs(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return if(viewHolder is EditCardViewHolder) super.getSwipeDirs(recyclerView, viewHolder) else 0
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return if(viewHolder is EditCardViewHolder && target is EditCardViewHolder) {
                if(deck.cards.remove(viewHolder.card)) {
                    deck.cards.add(target.adapterPosition - 1, viewHolder.card)
                    editViewAdapter.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
                } else {
                    Log.e("EditDeckFragment", "Tried to move card not in deck")
                }
                true
            } else {
                false
            }
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            if(viewHolder is EditCardViewHolder && direction == ItemTouchHelper.RIGHT) {
                deck.cards.remove(viewHolder.card)
                editViewAdapter.notifyItemRemoved(viewHolder.adapterPosition)
            }
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(
                c,
                recyclerView,
                viewHolder,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )

            var icon = deleteIcon!!
            var background = deleteBackground

            // Copy-pasted from: https://medium.com/@zackcosborn/step-by-step-recyclerview-swipe-to-delete-and-undo-7bbae1fce27e
            val itemView = viewHolder.itemView
            val backgroundCornerOffset = 20

            val iconMargin = (itemView.height - icon.intrinsicHeight) / 2
            val iconTop =
                itemView.top + (itemView.height - icon.intrinsicHeight) / 2
            val iconBottom = iconTop + icon.intrinsicHeight

            if (dX > 0) { // Swiping to the right
                val iconLeft = itemView.left + iconMargin
                val iconRight = itemView.left + iconMargin + icon.intrinsicWidth

                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                background.setBounds(
                    itemView.left, itemView.top,
                    itemView.left + dX.toInt() + backgroundCornerOffset,
                    itemView.bottom
                )
            } else if (dX < 0) { // Swiping to the left

                val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                val iconRight = itemView.right - iconMargin
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                background.setBounds(
                    itemView.right + dX.toInt() - backgroundCornerOffset,
                    itemView.top, itemView.right, itemView.bottom
                )
            } else { // view is unSwiped
                background.setBounds(0, 0, 0, 0)
            }

            background.draw(c)
            icon.draw(c)
        }

        override fun isLongPressDragEnabled() = false

    }

    abstract inner class EditViewHolder(view: View) : RecyclerView.ViewHolder(view)

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

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

            })

            description.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    deck.description = s.toString()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

            })
        }

    }

    inner class EditCardViewHolder(view: View) : EditViewHolder(view) {
        private val prompt: EditText = view.findViewById(R.id.edit_card_prompt)
        private val text: EditText = view.findViewById(R.id.edit_card_text)
        private val dragHandle : ImageView = view.findViewById(R.id.edit_card_drag_bar)

        private val promptChangeListener = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                _card.prompt = s.toString()
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        }

        private val answerChangeListener = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                _card.answer = s.toString()
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        }

        private val dragStartListener = { _: View?, event: MotionEvent? ->
            if (event?.actionMasked == MotionEvent.ACTION_DOWN) {
                itemTouchHelper.startDrag(this@EditCardViewHolder)
                true
            } else {
                false
            }
        }

        init {
            prompt.addTextChangedListener(promptChangeListener)
            text.addTextChangedListener(answerChangeListener)
            dragHandle.setOnTouchListener(dragStartListener)
        }

        private lateinit var _card: MutableCard

        val card
            get() = _card

        fun setCard(card: MutableCard) {
            this._card = card
            prompt.setText(card.prompt)
            text.setText(card.answer)
        }
    }

    inner class AddCardViewHolder(view: View) : EditViewHolder(view) {
        val button: Button = view.findViewById(R.id.add_item_button)
    }

    inner class EditViewAdapter : RecyclerView.Adapter<EditViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditViewHolder {
            val inflater = LayoutInflater.from(requireActivity())
            return when (viewType) {
                R.layout.edit_deck_list_item -> EditDeckViewHolder(
                    inflater.inflate(
                        viewType,
                        parent,
                        false
                    )
                )
                R.layout.edit_card_list_item -> EditCardViewHolder(
                    inflater.inflate(
                        viewType,
                        parent,
                        false
                    )
                )
                R.layout.edit_add_list_item -> AddCardViewHolder(
                    inflater.inflate(
                        viewType,
                        parent,
                        false
                    )
                )
                else -> throw IllegalArgumentException("Invalid view type")
            }
        }

        override fun onBindViewHolder(holder: EditViewHolder, position: Int) {
            when (holder) {
                is EditDeckViewHolder -> {
                    holder.setDeck(deck)
                }
                is EditCardViewHolder -> {
                    holder.setCard(deck.cards[position - 1])
                }
                is AddCardViewHolder -> {
                    holder.button.setOnClickListener {
                        deck.cards.add(MutableCard(Card(0L, deck.deckId, "", "", 0)))
                        notifyItemInserted(deck.cards.size)
                    }
                }
            }
        }

        override fun getItemViewType(pos: Int): Int {
            return when (pos) {
                0 -> R.layout.edit_deck_list_item
                deck.cardCount + 1 -> R.layout.edit_add_list_item
                else -> R.layout.edit_card_list_item
            }
        }

        override fun getItemCount(): Int = deck.cardCount + 2

    }

}