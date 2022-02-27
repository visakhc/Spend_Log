package com.app.spendlog.ui
//TODO Add an mBudget increment variable for easy access
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.app.spendlog.R
import com.app.spendlog.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private var mBudget = 0
    private var binding: ActivityHomeBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        init()
    }

    private fun init() {
        setBudget()
        handleEvents()
    }

    private fun setBudget() {
        binding?.tvBudgetView?.text = mBudget.toString()
        binding?.tvBudget?.text = mBudget.toString()

    }

    private fun handleEvents() {
        binding?.cvBudget?.setOnClickListener {
            binding?.cvAdd?.visibility = View.VISIBLE
        }
        binding?.homeLayout?.setOnClickListener {
            if (binding?.cvAdd?.visibility == View.VISIBLE) {
                binding?.cvAdd?.visibility = View.GONE
            }
        }
        binding?.ivPlus?.setOnClickListener {
            mBudget += 500
            setBudget()

        }
        binding?.ivMinus?.setOnClickListener {
            mBudget -= 500
            setBudget()

        }

    }
}