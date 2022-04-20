package ru.pashaginas.myapplication

import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        var costId = intent.getIntExtra("COST_ID", 0)
        Log.e("TAG", "Cost Id = " + costId)
    }
}
