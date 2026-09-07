package com.kite.mnemoai.ui.widget.wheelwidget

/**
 * List 数据源的滚轮适配器。
 * @param items 数据列表
 * @param formatter 每个元素的显示文本；默认 toString()，可自定义（如日期补零）。
 */
class ListWheelAdapter<T>(
    private val items: List<T>,
    private val formatter: (T) -> String = { it.toString() },
) : WheelAdapter {
    override val itemCount get() = items.size

    override fun getItemText(position: Int) = formatter(items[position])
}
