package com.quizlingo.verticalprototype

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DatabasePrototypeActivity : AppCompatActivity() {

    data class DeckItemModel(val type: ModelType, val data: DatabaseComponent.Deck?) {
        enum class ModelType {
            DECK, ADD_BUTTON
        }
    }

    open class CustomViewHolder(heldView: View) : RecyclerView.ViewHolder(heldView)

    class DeckItemViewHolder(heldView: View) : CustomViewHolder(heldView) {
        private val view: ConstraintLayout = heldView as ConstraintLayout
        val name: TextView = view.findViewById(R.id.deck_name)
        val count: TextView = view.findViewById(R.id.deck_card_count)
        val description: TextView = view.findViewById(R.id.deck_description)

    }

    class AddItemViewHolder(heldView: View) : CustomViewHolder(heldView) {
        private val view: ConstraintLayout = heldView as ConstraintLayout
        val button: Button = view.findViewById(R.id.add_item_button)
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
                }
                DeckItemModel.ModelType.ADD_BUTTON -> {
                    val holder = holder as AddItemViewHolder
                    // TODO: add an onClickListener to holder.button that switches to the "creating a new deck" activity
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

    private lateinit var viewAdapter: DeckItemViewAdapter
    private lateinit var database: DatabaseComponent.AppDatabase
    private val data: MutableLiveData<List<DatabaseComponent.Deck>> = MutableLiveData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_database_prototype)

        database = DatabaseComponent(this).getDatabase()

        // FIXME: Instead of displaying an empty list, some sort of loading icon should be displayed until the list is loaded
        data.value = listOf()

        var recyclerView: RecyclerView = findViewById(R.id.deck_list_view)

        viewAdapter = DeckItemViewAdapter(data.value!!.map{DeckItemModel(DeckItemModel.ModelType.DECK, it)} + DeckItemModel(DeckItemModel.ModelType.ADD_BUTTON, null))
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = viewAdapter

        data.observe(this, Observer { items: List<DatabaseComponent.Deck> ->
            viewAdapter.updateData(items.map{DeckItemModel(DeckItemModel.ModelType.DECK, it)} + DeckItemModel(DeckItemModel.ModelType.ADD_BUTTON, null))
        })

        LoadDecksTask(database, data).execute()
    }
}
