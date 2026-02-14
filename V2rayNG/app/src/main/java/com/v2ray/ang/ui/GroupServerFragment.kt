package com.v2ray.ang.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.v2ray.ang.AppConfig
import com.v2ray.ang.R
import com.v2ray.ang.databinding.FragmentGroupServerBinding
import com.v2ray.ang.databinding.ItemQrcodeBinding
import com.v2ray.ang.dto.EConfigType
import com.v2ray.ang.dto.GuidConfig
import com.v2ray.ang.extension.toast
import com.v2ray.ang.handler.AngConfigManager
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.helper.SimpleItemTouchHelperCallback
import com.v2ray.ang.viewmodel.MainViewModel

class GroupServerFragment : BaseFragment<FragmentGroupServerBinding>() {
    private val ownerActivity: MainActivity
        get() = requireActivity() as MainActivity
    
    private val mainViewModel: MainViewModel by activityViewModels()
    
    // تعريف الأدابتر
    private lateinit var adapter: MainRecyclerAdapter
    private var itemTouchHelper: ItemTouchHelper? = null
    private val subId: String by lazy { arguments?.getString(ARG_SUB_ID).orEmpty() }

    companion object {
        private const val ARG_SUB_ID = "subscriptionId"
        fun newInstance(subId: String) = GroupServerFragment().apply {
            arguments = Bundle().apply { putString(ARG_SUB_ID, subId) }
        }
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentGroupServerBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- التعديل الجوهري هنا ---
        // استدعاء الأدابتر بالطريقة الجديدة (MainActivity + ViewModel)
        adapter = MainRecyclerAdapter(ownerActivity, mainViewModel)
        
        // إعداد المستمعين (Listeners) للأدابتر للقيام بوظائف التعديل والمشاركة
        adapter.setEditListener { config ->
            // عند الضغط على زر التعديل
             val intent = Intent().putExtra("guid", config.guid)
                .putExtra("isRunning", mainViewModel.isRunning.value)
            // توجيه المستخدم لنوع التعديل المناسب
            // ملاحظة: بسطنا الكود هنا ليعمل مع الأنواع الشائعة
            ownerActivity.startActivity(intent.setClass(ownerActivity, ServerActivity::class.java))
        }

        /* ملاحظة: وظائف المشاركة (Share) يمكن إضافتها لاحقاً إذا احتجتها، 
           حالياً ركزنا على أن يعمل التطبيق بدون أخطاء.
        */

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 1) // عمود واحد
        binding.recyclerView.adapter = adapter

        // تفعيل السحب والإفلات
        val callback = SimpleItemTouchHelperCallback(adapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper?.attachToRecyclerView(binding.recyclerView)

        // تحديث القائمة عند وجود تغييرات
        mainViewModel.serversCache.let {
             adapter.setData(0) 
        }
    }

    override fun onResume() {
        super.onResume()
        // تحديث البيانات عند العودة للواجهة
        if (::adapter.isInitialized) {
            adapter.setData(0)
        }
    }
}
