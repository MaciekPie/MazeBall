package com.example.mazeball

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mazeball.ui.theme.MazeBallTheme


import android.content.Intent
import android.widget.Button


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // added
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.startButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)

        startButton.setOnClickListener {
            // przejście do ekranu gry
            // Toast.makeText(this, "Starting game...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }

        settingsButton.setOnClickListener {
            // TODO: dodaj ekran ustawień w przyszłości
        }

        /*
        enableEdgeToEdge()
        setContent {
            MazeBallTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }*/
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MazeBallTheme {
        Greeting("Android")
    }
}
