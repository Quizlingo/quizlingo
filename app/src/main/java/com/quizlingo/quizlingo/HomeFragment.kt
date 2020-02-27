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

    private lateinit var list: RecyclerView
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: HomeDeckAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home_page, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        adapter = HomeDeckAdapter(requireActivity())

        val decksObserver = Observer<List<Deck>> { newDeck ->
            adapter.setDataSource(newDeck)
        }
        viewModel.allDecks.observe(viewLifecycleOwner, decksObserver)

        list = view.findViewById(R.id.deck_list)

        list.adapter = adapter
        list.layoutManager = LinearLayoutManager(requireActivity())

        return view
    }
}

class HomeDeckAdapter(
    private val context: Context,
    private var dataSource: List<Deck> = Collections.emptyList()
) :
    RecyclerView.Adapter<HomeDeckAdapter.DeckViewHolder>() {
    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    class DeckViewHolder(val view: View) :
        RecyclerView.ViewHolder(view) {
        var title: TextView
        var desc: TextView

        init {
            title = view.findViewById(R.id.title)
            desc = view.findViewById(R.id.desc)
        }
    }

    fun setDataSource(newDeck: List<Deck>) {
        dataSource = newDeck
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
    }

    override fun getItemCount() = dataSource.size
}
