package com.v2ray.ang.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.v2ray.ang.R
import com.v2ray.ang.databinding.ItemRecyclerMainBinding
import com.v2ray.ang.dto.GuidConfig
import com.v2ray.ang.extension.toast
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.helper.SimpleItemTouchHelperCallback
import com.v2ray.ang.util.Utils

class MainRecyclerAdapter(private val activity: MainActivity) : RecyclerView.Adapter<MainRecyclerAdapter.BaseViewHolder>(), SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {
    private var shareListener: ((GuidConfig) -> Unit)? = null
    private var editListener: ((GuidConfig) -> Unit)? = null

    override fun getItemCount() = activity.mainViewModel.serversCache.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is MainViewHolder) {
            val guid = activity.mainViewModel.serversCache[position].guid
            val config = activity.mainViewModel.serversCache[position].config
            // تعديل: استخدام الألوان الجديدة
            val affix = MmkvManager.decodeServerAffix(guid)

            // 1. تعيين الاسم (موجود في تصميمك)
            holder.itemBinding.tvName.text = config.remarks

            // 2. تعيين نتيجة الفحص (موجود في تصميمك)
            holder.itemBinding.tvTestResult.text = affix?.getTestDelayString() ?: ""
            if (affix?.testDelayMillis ?: 0L < 0) {
                 holder.itemBinding.tvTestResult.setTextColor(ContextCompat.getColor(activity, R.color.red))
            } else {
                 holder.itemBinding.tvTestResult.setTextColor(ContextCompat.getColor(activity, R.color.secondary_text))
            }

            // 3. تعيين لون المؤشر الجانبي (v_indicator) بدلاً من تغيير خلفية الكارد
            // هذا الجزء مهم ليعرف المستخدم السيرفر المختار
            if (guid == activity.mainViewModel.mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER)) {
                holder.itemBinding.vIndicator.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorAccent))
                holder.itemBinding.tvName.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary))
            } else {
                holder.itemBinding.vIndicator.setBackgroundColor(Color.TRANSPARENT)
                holder.itemBinding.tvName.setTextColor(ContextCompat.getColor(activity, R.color.primary_text))
            }

            // 4. تشغيل حدث الضغط على السيرفر
            holder.itemView.setOnClickListener {
                activity.mainViewModel.updateConfigViaSub(guid)
                val selected = MmkvManager.decodeServerAffix(activity.mainViewModel.mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER))
                if (selected != null) {
                    notifyItemChanged(activity.mainViewModel.serversCache.indexOfFirst { it.guid == selected.guid })
                }
                notifyItemChanged(position)
                activity.reloadServerList()
            }

            // 5. زر التعديل (iv_edit) - موجود في تصميمك
            holder.itemBinding.ivEdit.setOnClickListener {
                editListener?.invoke(activity.mainViewModel.serversCache[position])
            }
            
            // تم حذف الأكواد التي تشير إلى items غير موجودة مثل tvStatistics و layoutShare
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
            // itemView.setBackgroundColor(Color.LTGRAY) // اختياري
        }

        fun onItemClear() {
            // itemView.setBackgroundColor(0) // اختياري
        }
    }

    class MainViewHolder(val itemBinding: ItemRecyclerMainBinding) : BaseViewHolder(itemBinding.root)

    fun setShareListener(listener: (GuidConfig) -> Unit) {
        this.shareListener = listener
    }

    fun setEditListener(listener: (GuidConfig) -> Unit) {
        this.editListener = listener
    }
}
