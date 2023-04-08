package com.example.voicerecorddemo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.voicerecorddemo.R
import com.example.voicerecorddemo.bean.RecorderInfo
import com.example.voicerecorddemo.untils.ScreenUntil
import kotlin.math.roundToInt

/**
 * @Description:
 * @Author: zouji
 * @CreateDate: 2023/4/8 09:48
 */
class RecorderAdapter(private val context: Context, private val data: ArrayList<RecorderInfo>) :
    RecyclerView.Adapter<RecorderAdapter.RecordHolder>() {

    private var itemClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_recoder, parent, false)
        return RecordHolder(view)
    }

    override fun onBindViewHolder(holder: RecordHolder, position: Int) {
        val duration = (data[position].time.toFloat() / 1000).roundToInt()
        holder.tvDuration?.text = String.format(
            context.resources.getString(R.string.voice_duration),
            duration
        )
        val minWidth = (ScreenUntil.widthPixels * 0.2f).toInt()  //最小宽度为屏幕宽度的20%
        val maxWidth = (ScreenUntil.widthPixels * 0.6f).toInt()  //最大宽度为屏幕宽度的60%
        holder.llRecorderLength?.apply {
            layoutParams?.width = (minWidth + (maxWidth / 60f) * duration).toInt()  //设置语音框宽度
            //点击事件
            setOnClickListener {
                itemClickListener?.onItemClick(it, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class RecordHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDuration = itemView.findViewById<TextView>(R.id.tv_voice_duration)
        val llRecorderLength = itemView.findViewById<LinearLayout>(R.id.ll_recoder_length)
    }

    /**
     * 添加录音数据
     * @param info
     */
    fun addData(info: RecorderInfo) {
        data.add(info)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): RecorderInfo {
        return data[position]
    }

    /**
     * 设置item点击事件监听
     * @param itemClickListener
     */
    fun setOnItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }
}
