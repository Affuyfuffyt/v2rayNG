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
import com.v2ray.ang.dto.EConfigType
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.helper.SimpleItemTouchHelperCallback
import com.v2ray.ang.viewmodel.MainViewModel

// الآن هذا الكلاس ينفذ الواجهة المطلوبة ItemTouchHelperAdapter بشكل صحيح
class MainRecyclerAdapter(
    private val activity: MainActivity, 
    private val viewModel: MainViewModel
) : RecyclerView.Adapter<MainRecyclerAdapter.BaseViewHolder>(), 
    SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {
    
    // تعريف المستمع لزر التعديل
    private var editListener: ((GuidConfig) -> Unit)? = null

    fun setEditListener(listener: (GuidConfig) -> Unit) {
        this.editListener = listener
    }

    // دالة لتحديث البيانات
    fun setData(newSize: Int) {
        notifyDataSetChanged()
    }

    override fun getItemCount() = viewModel.serversCache.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is MainViewHolder) {
            val guidConfig = viewModel.serversCache[position]
            val guid = guidConfig.guid
            val config = guidConfig.config
            val affix = MmkvManager.decodeServerAffix(guid)

            // 1. الاسم
            holder.itemBinding.tvName.text = config.remarks

            // 2. نتيجة الفحص (Ping)
            holder.itemBinding.tvTestResult.text = affix?.getTestDelayString() ?: ""
            if (affix?.testDelayMillis ?: 0L < 0) {
                 holder.itemBinding.tvTestResult.setTextColor(ContextCompat.getColor(activity, R.color.red))
            } else {
                 holder.itemBinding.tvTestResult.setTextColor(ContextCompat.getColor(activity, R.color.secondary_text))
            }

            // 3. المؤشر الجانبي (الأزرق)
            val selectedGuid = MmkvManager.getSelectServer()
            if (guid == selectedGuid) {
                holder.itemBinding.vIndicator.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorAccent))
                holder.itemBinding.tvName.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary))
            } else {
                holder.itemBinding.vIndicator.setBackgroundColor(Color.TRANSPARENT)
                holder.itemBinding.tvName.setTextColor(ContextCompat.getColor(activity, R.color.primary_text))
            }

            // 4. عند الضغط على السيرفر (اختيار)
            holder.itemView.setOnClickListener {
                MmkvManager.setSelectServer(guid)
                notifyDataSetChanged()
                activity.reloadServerList()
            }

            // 5. زر التعديل (القلم)
            holder.itemBinding.ivEdit.setOnClickListener {
                editListener?.invoke(guidConfig)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return MainViewHolder(ItemRecyclerMainBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    // دوال السحب والإفلات المطلوبة
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
        fun onItemSelected() {
             // اختياري: تغيير لون الخلفية عند السحب
        }

        fun onItemClear() {
             // اختياري: إعادة اللون الطبيعي
        }
    }

    class MainViewHolder(val itemBinding: ItemRecyclerMainBinding) : BaseViewHolder(itemBinding.root)
    }
