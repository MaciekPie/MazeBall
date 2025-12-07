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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_select)

        storage = LevelStorage(this)

        val listView = findViewById<ListView>(R.id.levelListView)
        val backButton = findViewById<Button>(R.id.backButton)
        val newLevelButton = findViewById<Button>(R.id.newLevelButton)

        val levels = storage.getLevels()

        // Na początek prosty adapter z nazwą "Level X"
        val items = levels.map { "Level ${it.id}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
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
}
