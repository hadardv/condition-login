package com.example.exe1mobilesecurity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exe1mobilesecurity.adapters.ConditionAdapter
import com.example.exe1mobilesecurity.models.Condition

class MainActivity : AppCompatActivity() {

    private lateinit var conditionAdapter: ConditionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.conditionsList)
        recyclerView.layoutManager = LinearLayoutManager(this);

        val conditionList = listOf(
            Condition("You may only enter in 00:00", false),
            Condition("Prove you alive.. move!", false),
            Condition("Turn the screen off... then awaken it to proceed.", false),
            Condition("You must have taken a photo in the last hour.", false),
            Condition("Phrase spoken: שומשום הפתח", false),
        )

        conditionAdapter = ConditionAdapter(conditionList.toMutableList())
        recyclerView.adapter = conditionAdapter

        conditionAdapter.updateCondition(0,true)
    }
}