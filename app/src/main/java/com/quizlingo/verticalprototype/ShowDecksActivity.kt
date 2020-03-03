package com.quizlingo.verticalprototype

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ShowDecksActivity : AppCompatActivity() {

    data class DeckItemModel(val type: ModelType, val data: DatabaseComponent.Deck?) {
        enum class ModelType {
            DECK, ADD_BUTTON
        }
    }

    open class CustomViewHolder(heldView: View) : RecyclerView.ViewHolder(heldView)

    class DeckItemViewHolder(heldView: View) : CustomViewHolder(heldView) {
        val root: ConstraintLayout = heldView as ConstraintLayout
        val name: TextView = root.findViewById(R.id.deck_name)
        val count: TextView = root.findViewById(R.id.deck_card_count)
        val description: TextView = root.findViewById(R.id.deck_description)

    }

    class AddItemViewHolder(heldView: View) : CustomViewHolder(heldView) {
        val root: ConstraintLayout = heldView as ConstraintLayout
        val button: Button = root.findViewById(R.id.add_item_button)
    }

    class DeckItemViewAdapter(var data: List<DeckItemModel>) : RecyclerView.Adapter<CustomViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            var inflater: LayoutInflater = LayoutInflater.from(parent.context)
            return when(DeckItemModel.ModelType.values()[viewType]) {
                DeckItemModel.ModelType.DECK -> {
                    val view = inflater.inflate(R.layout.deck_list_item, parent, false)
                    DeckItemViewHolder(view)
                }
                DeckItemModel.ModelType.ADD_BUTTON -> {
                    val view = inflater.inflate(R.layout.add_list_item, parent ,false)
                    AddItemViewHolder(view)
                }
            }
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            val item = data[position]

            when(item.type) {
                DeckItemModel.ModelType.DECK -> {
                    val holder = holder as DeckItemViewHolder
                    holder.name.text = item.data!!.deckName
                    holder.count.text = holder.count.resources.getQuantityString(R.plurals.cards, item.data!!.deckCardCount, item.data!!.deckCardCount)
                    holder.description.text = item.data!!.deckDescription
                    holder.root.setOnClickListener{
                        val intent = Intent(holder.root.context, EditDeckActivity::class.java)
                        intent.putExtra(EditDeckActivity.editModeKey, EditDeckActivity.EditModes.EDIT)
                        intent.putExtra(EditDeckActivity.editDeckIdKey, item.data!!.id)
                        holder.root.context.startActivity(intent)
                    }

                }
                DeckItemModel.ModelType.ADD_BUTTON -> {
                    val holder = holder as AddItemViewHolder
                    holder.button.setOnClickListener {
                        val intent = Intent(holder.button.context, EditDeckActivity::class.java)
                        intent.putExtra(EditDeckActivity.editModeKey, EditDeckActivity.EditModes.NEW)
                        holder.button.context.startActivity(intent)
                    }
                }
            }
        }

        override fun getItemCount(): Int = data.size

        override fun getItemViewType(pos: Int) = data[pos].type.ordinal

        fun updateData(data: List<DeckItemModel>) {
            this.data = data
            notifyDataSetChanged()
        }
    }

    class ShowDecksViewModel : ViewModel() {
        var data: MutableLiveData<List<DatabaseComponent.Deck>> = MutableLiveData<List<DatabaseComponent.Deck>>().also{it.value = mutableListOf()}
    }

    private lateinit var viewAdapter: DeckItemViewAdapter
    private lateinit var viewModel: ShowDecksViewModel
    private lateinit var database: DatabaseComponent.AppDatabase

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_deck)

        viewModel = ViewModelProvider(this)[ShowDecksViewModel::class.java]

        database = DatabaseComponent.getDatabase(this)

        recyclerView = findViewById(R.id.deck_list_view)
        progressBar = findViewById(R.id.deck_list_loading_bar)

        viewAdapter = DeckItemViewAdapter(viewModel.data.value!!.map{DeckItemModel(DeckItemModel.ModelType.DECK, it)} + DeckItemModel(DeckItemModel.ModelType.ADD_BUTTON, null))
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = viewAdapter

        viewModel.data.observe(this, Observer { items: List<DatabaseComponent.Deck> ->
            viewAdapter.updateData(items.map{DeckItemModel(DeckItemModel.ModelType.DECK, it)} + DeckItemModel(DeckItemModel.ModelType.ADD_BUTTON, null))
        })

    }

    override fun onResume() {
        super.onResume()

        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        database.deckDao().getDecks().observe(this, Observer {
            viewModel.data.value = it

            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        })
    }

}
