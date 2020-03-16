package com.quizlingo.quizlingo

import android.R.anim
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quizlingo.quizlingo.businesslogic.Deck


class HomeFragment : Fragment() {

    inner class HomeDeckAdapter(
        private var dataSource: List<Deck> = listOf()
    ) : RecyclerView.Adapter<HomeDeckAdapter.HomePageViewHolder>() {
        private val inflater: LayoutInflater = LayoutInflater.from(requireActivity())

        abstract inner class HomePageViewHolder(view: View) : RecyclerView.ViewHolder(view)

        inner class DeckViewHolder(var view: View) :
            HomePageViewHolder(view),
            View.OnClickListener {

            val title: TextView = view.findViewById(R.id.title)
            val desc: TextView = view.findViewById(R.id.desc)

            init {
                view.setOnClickListener(this)
            }

            lateinit var deck: Deck

            override fun onClick(v: View?) {
                viewModel.currentDeck.value = deck
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.content, StudyFragment.getInstance())
                    addToBackStack(null)
                    commit()
                }
            }
        }

        inner class AddDeckViewHolder(view: View): HomePageViewHolder(view) {
            val button: Button = view.findViewById(R.id.add_item_button)
        }

        fun setDataSource(newDeck: List<Deck>) {
            dataSource = newDeck
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): HomePageViewHolder {
            return when(viewType) {
                R.layout.home_deck_list_item -> DeckViewHolder(inflater.inflate(R.layout.home_deck_list_item, parent, false))
                R.layout.edit_add_list_item -> AddDeckViewHolder(inflater.inflate(R.layout.edit_add_list_item, parent, false))
                else -> throw IllegalArgumentException()
            }
        }

        override fun onBindViewHolder(holder: HomePageViewHolder, position: Int) {
            when(holder) {
                is DeckViewHolder -> {
                    val deck: Deck = dataSource[position]

                    holder.title.text = deck.title
                    holder.desc.text = deck.description
                    holder.deck = dataSource[position]
                }
                is AddDeckViewHolder -> {
                    holder.button.setOnClickListener {
                        viewModel.currentDeck.value = Deck(0L, "", "", listOf())
                        parentFragmentManager.beginTransaction().apply {
                            replace(R.id.content, EditDeckFragment.getInstance())
                            addToBackStack(null)
                            commit()
                        }
                    }
                }
            }


        }

        override fun getItemCount() = dataSource.size + 1

        override fun getItemViewType(position: Int): Int {
            return if(position < dataSource.size) R.layout.home_deck_list_item else R.layout.edit_add_list_item
        }
    }


    private lateinit var list: RecyclerView
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: HomeDeckAdapter

    companion object {
        fun getInstance() = HomeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home_page, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        adapter = HomeDeckAdapter(viewModel.allDecks.value ?: listOf())

        viewModel.allDecks.observe(viewLifecycleOwner, Observer {
            adapter.setDataSource(it)
        })

        viewModel.currentDeck.observe(viewLifecycleOwner, Observer {
            adapter.notifyDataSetChanged()
        })

        list = view.findViewById(R.id.deck_list)
        list.adapter = adapter
        list.layoutManager = LinearLayoutManager(requireActivity())

        val itemTouchHelper = ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            private val editIcon = requireActivity().getDrawable(R.drawable.ic_edit_white_24dp)
            private val editBackground = ColorDrawable(Color.BLUE)
            private val deleteIcon = requireActivity().getDrawable(R.drawable.ic_delete_white_24dp)
            private val deleteBackground = ColorDrawable(Color.RED)

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return if(viewHolder is HomeDeckAdapter.AddDeckViewHolder) 0 else super.getSwipeDirs(recyclerView, viewHolder)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if(viewHolder is HomeDeckAdapter.DeckViewHolder) {
                    val deck = viewHolder.deck
                    if(direction == ItemTouchHelper.LEFT) {
                        viewModel.currentDeck.value = deck
                        parentFragmentManager.beginTransaction().apply {
                            setCustomAnimations(R.anim.right_in, R.anim.left_out)
                            replace(R.id.content, EditDeckFragment.getInstance())
                            addToBackStack(null)
                            commit()
                        }
                    } else if(direction == ItemTouchHelper.RIGHT) {
                        viewModel.deleteDeck(deck)
                        adapter.notifyItemRemoved(viewHolder.adapterPosition)
                    }

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

                var icon = editIcon!!
                var background = editBackground

                // Copy-pasted from: https://medium.com/@zackcosborn/step-by-step-recyclerview-swipe-to-delete-and-undo-7bbae1fce27e
                val itemView = viewHolder.itemView
                val backgroundCornerOffset = 20

                if (dX > 0) { // Swiping to the right
                    icon = deleteIcon!!
                    background = deleteBackground

                    val iconMargin = (itemView.height - icon.intrinsicHeight) / 2
                    val iconTop =
                        itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                    val iconBottom = iconTop + icon.intrinsicHeight

                    val iconLeft = itemView.left + iconMargin
                    val iconRight = itemView.left + iconMargin + icon.intrinsicWidth
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    background.setBounds(
                        itemView.left, itemView.top,
                        itemView.left + dX.toInt() + backgroundCornerOffset,
                        itemView.bottom
                    )
                } else if (dX < 0) { // Swiping to the left
                    val iconMargin = (itemView.height - icon.intrinsicHeight) / 2
                    val iconTop =
                        itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                    val iconBottom = iconTop + icon.intrinsicHeight

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

        })
        itemTouchHelper.attachToRecyclerView(list)

    }
}

