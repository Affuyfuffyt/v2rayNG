package com.v2ray.ang.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.v2ray.ang.R
import com.v2ray.ang.databinding.ItemRecyclerMainBinding
import com.v2ray.ang.dto.ProfileItem
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.helper.SimpleItemTouchHelperCallback
import com.v2ray.ang.viewmodel.MainViewModel

// تعريف الكلاس مع تنفيذ الواجهة الصحيحة
class MainRecyclerAdapter(
    private val activity: MainActivity, 
    private val viewModel: MainViewModel
) : RecyclerView.Adapter<MainRecyclerAdapter.BaseViewHolder>(), 
    SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {
    
    // تعريف المستمع للتعديل (يستقبل GUID و ProfileItem)
    private var editListener: ((String, ProfileItem) -> Unit)? = null

    fun setEditListener(listener: (String, ProfileItem) -> Unit) {
        this.editListener = listener
    }

    fun setData(newSize: Int) {
        notifyDataSetChanged()
    }

    override fun getItemCount() = viewModel.serversCache.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is MainViewHolder) {
            // 1. استخراج البيانات بشكل صحيح (الحل الجذري للأخطاء)
            val serverCache = viewModel.serversCache[position]
            val guid = serverCache.guid
            val config = serverCache.config // هذا هو ProfileItem
            
            // 2. تعيين الاسم (حل مشكلة remarks)
            holder.itemBinding.tvName.text = config.remarks

            // 3. تعيين البينغ (بشكل آمن)
            // ملاحظة: تم تبسيط هذا الجزء لتجنب خطأ decodeServerAffix إذا كان غير موجود
            val testResult = MmkvManager.decodeServerAffix(guid)
            if (testResult != null) {
                holder.itemBinding.tvTestResult.text = testResult.getTestDelayString()
                if (testResult.testDelayMillis < 0) {
                     holder.itemBinding.tvTestResult.setTextColor(ContextCompat.getColor(activity, R.color.red))
                } else {
                     holder.itemBinding.tvTestResult.setTextColor(ContextCompat.getColor(activity, R.color.secondary_text))
                }
            } else {
                holder.itemBinding.tvTestResult.text = ""
            }

            // 4. تعيين لون الاختيار
            val selectedGuid = MmkvManager.getSelectServer()
            if (guid == selectedGuid) {
                holder.itemBinding.vIndicator.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorAccent))
                holder.itemBinding.tvName.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary))
            } else {
                holder.itemBinding.vIndicator.setBackgroundColor(Color.TRANSPARENT)
                holder.itemBinding.tvName.setTextColor(ContextCompat.getColor(activity, R.color.primary_text))
            }

            // 5. عند الضغط على السيرفر (اختيار)
            holder.itemView.setOnClickListener {
                MmkvManager.setSelectServer(guid)
                notifyDataSetChanged()
                // تم حذف reloadServerList لأنها غير موجودة، الفيو موديل سيتكفل بالتحديث
                // إذا لزم الأمر يمكن إضافة: viewModel.reloadServerList()
            }

            // 6. عند الضغط على زر التعديل
            holder.itemBinding.ivEdit.setOnClickListener {
                // نمرر البيانات الصحيحة للمستمع
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
