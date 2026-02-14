package com.v2ray.ang.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.v2ray.ang.R
import com.v2ray.ang.databinding.ItemRecyclerMainBinding
import com.v2ray.ang.dto.ProfileItem // هذا هو الكلاس الصحيح
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.helper.SimpleItemTouchHelperCallback
import com.v2ray.ang.viewmodel.MainViewModel

class MainRecyclerAdapter(
    private val activity: MainActivity, 
    private val viewModel: MainViewModel
) : RecyclerView.Adapter<MainRecyclerAdapter.BaseViewHolder>(), 
    SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {
    
    private var editListener: ((String, ProfileItem) -> Unit)? = null

    // نمرر الـ GUID والـ ProfileItem للمستمع
    fun setEditListener(listener: (String, ProfileItem) -> Unit) {
        this.editListener = listener
    }

    fun setData(newSize: Int) {
        notifyDataSetChanged()
    }

    override fun getItemCount() = viewModel.serversCache.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is MainViewHolder) {
            val guid = viewModel.serversCache[position].guid
            val config = viewModel.serversCache[position] // هذا هو ProfileItem
            val affix = MmkvManager.decodeServerAffix(guid)

            // 1. الاسم
            holder.itemBinding.tvName.text = config.remarks

            // 2. البينغ
            holder.itemBinding.tvTestResult.text = affix?.getTestDelayString() ?: ""
            if (affix?.testDelayMillis ?: 0L < 0) {
                 holder.itemBinding.tvTestResult.setTextColor(ContextCompat.getColor(activity, R.color.red))
            } else {
                 holder.itemBinding.tvTestResult.setTextColor(ContextCompat.getColor(activity, R.color.secondary_text))
            }

            // 3. المؤشر الملون
            val selectedGuid = MmkvManager.getSelectServer()
            if (guid == selectedGuid) {
                holder.itemBinding.vIndicator.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorAccent))
                holder.itemBinding.tvName.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary))
            } else {
                holder.itemBinding.vIndicator.setBackgroundColor(Color.TRANSPARENT)
                holder.itemBinding.tvName.setTextColor(ContextCompat.getColor(activity, R.color.primary_text))
            }

            // 4. الاختيار
            holder.itemView.setOnClickListener {
                MmkvManager.setSelectServer(guid)
                notifyDataSetChanged()
                activity.reloadServerList()
            }

            // 5. التعديل
            holder.itemBinding.ivEdit.setOnClickListener {
                editListener?.invoke(guid, config)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return MainViewHolder(ItemRecyclerMainBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        viewModel.swapServer(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {
        viewModel.removeServer(viewModel.serversCache[position].guid)
        notifyItemRemoved(position)
    }

    open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onItemSelected() {}
        fun onItemClear() {}
    }

    class MainViewHolder(val itemBinding: ItemRecyclerMainBinding) : BaseViewHolder(itemBinding.root)
    }
