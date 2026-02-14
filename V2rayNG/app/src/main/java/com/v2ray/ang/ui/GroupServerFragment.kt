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

    companion object {
        private const val ARG_SUB_ID = "subscriptionId"
        fun newInstance(subId: String): GroupServerFragment {
            return GroupServerFragment().apply {
                arguments = Bundle().apply { putString(ARG_SUB_ID, subId) }
            }
        }
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentGroupServerBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MainRecyclerAdapter(ownerActivity, mainViewModel)
        
        // التعديل هنا: نستقبل (guid, profileItem)
        adapter.setEditListener { guid, profileItem ->
             val intent = Intent().putExtra("guid", guid)
                .putExtra("isRunning", mainViewModel.isRunning.value)
             // يمكن استخدام profileItem لتحديد نوع الاكتيفيتي اذا اردت، لكن حالياً نوجهه للافتراضي
            ownerActivity.startActivity(intent.setClass(ownerActivity, ServerActivity::class.java))
        }

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
        binding.recyclerView.adapter = adapter

        val callback = SimpleItemTouchHelperCallback(adapter, false)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper?.attachToRecyclerView(binding.recyclerView)

        adapter.setData(0)
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            adapter.setData(0)
        }
    }
}
