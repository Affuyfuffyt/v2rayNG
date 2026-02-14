package com.v2ray.ang.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.v2ray.ang.databinding.FragmentGroupServerBinding
import com.v2ray.ang.helper.SimpleItemTouchHelperCallback
import com.v2ray.ang.viewmodel.MainViewModel
import com.v2ray.ang.ui.ServerActivity

class GroupServerFragment : BaseFragment<FragmentGroupServerBinding>() {
    private val ownerActivity: MainActivity
        get() = requireActivity() as MainActivity
    
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: MainRecyclerAdapter
    private var itemTouchHelper: ItemTouchHelper? = null

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentGroupServerBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. استدعاء الأدابتر (تأكد أنك حدثت ملف MainRecyclerAdapter مسبقاً)
        adapter = MainRecyclerAdapter(ownerActivity, mainViewModel)
        
        // 2. إعداد زر التعديل (القلم)
        adapter.setEditListener { config ->
             val intent = Intent().putExtra("guid", config.guid)
                .putExtra("isRunning", mainViewModel.isRunning.value)
            ownerActivity.startActivity(intent.setClass(ownerActivity, ServerActivity::class.java))
        }

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
        binding.recyclerView.adapter = adapter

        // 3. تصليح الخطأ هنا: إضافة false للمعامل الثاني
        val callback = SimpleItemTouchHelperCallback(adapter, false)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper?.attachToRecyclerView(binding.recyclerView)

        // 4. تحديث البيانات
        adapter.setData(0)
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            adapter.setData(0)
        }
    }
}
