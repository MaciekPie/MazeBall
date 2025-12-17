package com.example.mazeball

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity


class LevelSelectActivity : AppCompatActivity() {
    private lateinit var storage: LevelStorage
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private var levels: List<Level> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_select)

        storage = LevelStorage(this)

        listView = findViewById(R.id.levelListView)
        val backButton = findViewById<Button>(R.id.backButton)
        val newLevelButton = findViewById<Button>(R.id.newLevelButton)


        // pusty adapter na start, dane dociągniemy w onResume
        adapter = ArrayAdapter(this, R.layout.item_level, R.id.levelNameText, mutableListOf())
        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedLevel = levels[position]
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("LEVEL_ID", selectedLevel.id)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            finish()
        }

        newLevelButton.setOnClickListener {
            // Nowy level – start GameActivity bez LEVEL_ID, wtedy generuje nowy maze
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        reloadLevels()
    }

    private fun reloadLevels() {
        levels = storage.getLevels()
        val items = levels.map { "Level ${it.id}" }
        adapter.clear()
        adapter.addAll(items)
        adapter.notifyDataSetChanged()
    }
}
