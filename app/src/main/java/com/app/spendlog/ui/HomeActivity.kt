package com.app.spendlog.ui
//TODO Add an mBudget increment variable for easy access
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.spendlog.adapter.SpendAdapter
import com.app.spendlog.bottomsheets.AddSpendBottomSheet
import com.app.spendlog.databinding.ActivityHomeBinding
import com.app.spendlog.model.SpendModel

class HomeActivity : AppCompatActivity(), SpendAdapter.OnEachListener {
    private var mBudget = 0
    var modelList = mutableListOf<SpendModel>()
    private var binding: ActivityHomeBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        init()
    }

    private fun init() {
        setBudget()
        modelList.add(SpendModel("minus","200.0","21-02-2022","8:08pm"))
        setRecycler(modelList)
        handleEvents()

    }
     fun setRecycler(model: MutableList<SpendModel>) {
        binding?.spendRecycler?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = SpendAdapter(model, this@HomeActivity)
        }
    }

    private fun setBudget() {
        binding?.tvBudgetView?.text = mBudget.toString()
        binding?.tvBudget?.text = mBudget.toString()

    }

    private fun handleEvents() {
        binding?.inclLayout?.ivSettings?.setOnClickListener {
            recreate()
        }
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
        binding?.tvAddSpend?.setOnClickListener {
            AddSpendBottomSheet().show(supportFragmentManager, "addSpent")
        }

    }

    override fun onEachClick(position: Int) {
        Toast.makeText(this, "Hi----> $position", Toast.LENGTH_SHORT).show()
    }
}