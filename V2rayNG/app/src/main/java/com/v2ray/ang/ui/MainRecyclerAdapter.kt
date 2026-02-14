package com.v2ray.ang.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.v2ray.ang.R
import com.v2ray.ang.databinding.ItemRecyclerMainBinding
import com.v2ray.ang.dto.EConfigType
import com.v2ray.ang.extension.toast
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.helper.SimpleItemTouchHelperCallback
import com.v2ray.ang.util.Utils

class MainRecyclerAdapter(private val activity: MainActivity) : RecyclerView.Adapter<MainRecyclerAdapter.BaseViewHolder>(), SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {
    private var shareListener: ((String) -> Unit)? = null
    private var editListener: ((String) -> Unit)? = null

    override fun getItemCount() = activity.mainViewModel.serversCache.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is MainViewHolder) {
            val guid = activity.mainViewModel.serversCache[position].guid
            val config = activity.mainViewModel.serversCache[position].config
            // تعديل: استخدام الألوان الجديدة
            val affix = MmkvManager.decodeServerAffix(guid)

            // 1. تعيين الاسم
            holder.itemBinding.tvName.text = config.remarks

            // 2. تعيين نتيجة الفحص
            holder.itemBinding.tvTestResult.text = affix?.getTestDelayString() ?: ""
            if (affix?.testDelayMillis ?: 0L < 0) {
                 holder.itemBinding.tvTestResult.setTextColor(ContextCompat.getColor(activity, R.color.red))
            } else {
                 holder.itemBinding.tvTestResult.setTextColor(ContextCompat.getColor(activity, R.color.secondary_text))
            }

            // 3. تعيين لون المؤشر الجانبي
            val selectedGuid = activity.mainViewModel.mainStorage?.decodeString("KEY_SELECTED_SERVER")
            if (guid == selectedGuid) {
                holder.itemBinding.vIndicator.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorAccent))
                holder.itemBinding.tvName.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary))
            } else {
                holder.itemBinding.vIndicator.setBackgroundColor(Color.TRANSPARENT)
                holder.itemBinding.tvName.setTextColor(ContextCompat.getColor(activity, R.color.primary_text))
            }

            // 4. تشغيل حدث الضغط على السيرفر
            holder.itemView.setOnClickListener {
                activity.mainViewModel.updateConfigViaSub(guid)
                // تحديث الواجهة يدوياً
                notifyDataSetChanged()
                activity.reloadServerList()
            }

            // 5. زر التعديل
            holder.itemBinding.ivEdit.setOnClickListener {
                editListener?.invoke(guid)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return MainViewHolder(ItemRecyclerMainBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        activity.mainViewModel.swapServer(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {
        activity.mainViewModel.removeServer(activity.mainViewModel.serversCache[position].guid)
        notifyItemRemoved(position)
    }

    open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onItemSelected() {
            // itemView.setBackgroundColor(Color.LTGRAY)
        }

        fun onItemClear() {
            // itemView.setBackgroundColor(0)
        }
    }

    class MainViewHolder(val itemBinding: ItemRecyclerMainBinding) : BaseViewHolder(itemBinding.root)

    fun setShareListener(listener: (String) -> Unit) {
        this.shareListener = listener
    }

    fun setEditListener(listener: (String) -> Unit) {
        this.editListener = listener
    }
}
