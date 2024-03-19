package com.example.totp.ui

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.totp.R
import com.example.totp.bean.Verifier
import com.example.totp.databinding.ActivityMainBinding
import com.example.totp.databinding.ItemVerifierBinding
import com.example.totp.utils.DialogUtil.showAlertDialog
import com.example.totp.utils.Repository
import com.example.totp.utils.totp.TotpUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val verifierList = MutableLiveData(emptyList<Verifier>())

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnAdd.setOnClickListener {
            VerifierActivity.start(this@MainActivity, null)
        }
        verifierList.observe(this) {
            adapter.setList(it)
        }

        binding.rv.adapter = adapter
        adapter.setOnItemClickListener { _, _, position ->
            val verifier = adapter.getItem(position)
            VerifierActivity.start(this@MainActivity, verifier)
        }
        adapter.setOnItemLongClickListener { _, _, position ->
            val verifier = adapter.getItem(position)
            showAlertDialog("是否删除${verifier.name}?") {
                adapter.removeAt(position)
                Repository.deleteVerifier(verifier)
            }
            true
        }
    }

    private fun initVerifierList() = lifecycleScope.launch {
        verifierList.value = Repository.verifierList
    }

    override fun onResume() {
        super.onResume()
        initVerifierList()
    }

    private val adapter =
        object : BaseQuickAdapter<Verifier, BaseViewHolder>(R.layout.item_verifier) {
            private val progressBars = mutableListOf<ProgressBar>()

            init {
                lifecycleScope.launch {
                    while (true) {
                        delay(1000)
                        progressBars.forEach {
                            it.progress = TotpUtil.getLeftTime().toInt()
                        }
                    }
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                val holder = super.onCreateViewHolder(parent, viewType)
                val progressBar = holder.getView<ProgressBar>(R.id.progressBar)
                progressBars.add(progressBar)
                return holder
            }

            override fun convert(holder: BaseViewHolder, item: Verifier) {
                val itemBinding = ItemVerifierBinding.bind(holder.itemView)
                itemBinding.tvName.text = item.name
                itemBinding.tvVerifierCode.text = TotpUtil.generate(item.secret)
                itemBinding.progressBar.max = 30
//                itemBinding.progressBar.progress = TotpUtil.getLeftTime().toInt()
            }

        }
}