package com.quizlingo.quizlingo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quizlingo.quizlingo.businesslogic.Deck
import java.util.*


class HomeFragment : Fragment() {

    inner class HomeDeckAdapter(
        private var dataSource: List<Deck> = listOf()
    ) : RecyclerView.Adapter<HomeDeckAdapter.DeckViewHolder>() {
        private val inflater: LayoutInflater = LayoutInflater.from(requireActivity())

        inner class DeckViewHolder(var view: View) :
            RecyclerView.ViewHolder(view) {
            var title: TextView = view.findViewById(R.id.title)
            var desc: TextView

            init {
                title = view.findViewById(R.id.title)
                desc = view.findViewById(R.id.desc)
            }
        }

        fun setDataSource(newDeck: List<Deck>) {
            dataSource = newDeck
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): DeckViewHolder {
            val view: View = inflater.inflate(R.layout.deck_list_item, parent, false)
            return DeckViewHolder(view)
        }

        override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
            val deck: Deck = dataSource[position]

            holder.title.text = deck.title
            holder.desc.text = deck.description
            holder.view.setOnClickListener {
                val viewModel =
                    ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
                val deck = dataSource[position]
                viewModel.currentDeck.value = deck
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.content, Study.getInstance())
                    addToBackStack(null)
                    commit()
                }
            }

        }

        override fun getItemCount() = dataSource.size
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
    ): View? = inflater.inflate(R.layout.home_page, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        adapter = HomeDeckAdapter(viewModel.allDecks.value ?: listOf())

        viewModel.allDecks.observe(viewLifecycleOwner, Observer {
            adapter.setDataSource(it)
        })

        list = view.findViewById(R.id.deck_list)
        list.adapter = adapter
        list.layoutManager = LinearLayoutManager(requireActivity())

    }
}

