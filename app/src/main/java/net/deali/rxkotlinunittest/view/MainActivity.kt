package net.deali.rxkotlinunittest.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.deali.rxkotlinunittest.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}