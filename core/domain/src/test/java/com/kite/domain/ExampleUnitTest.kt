package com.kite.domain

import org.junit.Test

import org.junit.Assert.*
import java.time.LocalDate

/**
 * MemoryAlgorithm 单元测试
 *
 * 规格来源：README「记忆算法」章节 + MemoryAlgorithm.java
 * 注意：learningTime 参数单位为「毫秒」（调用方传入 SystemClock.elapsedRealtime() 差值），
 *       阈值 10_000/30_000/60_000 即 10s/30s/60s。
 */
class MemoryAlgorithmTest {

    private val today: LocalDate = LocalDate.of(2026, 1, 1)

    private fun assertNextDate(
        reviewCount: Int, blurCount: Int, forgetCount: Int,
        learningTimeMs: Long, expectedDays: Long
    ) {
        val result = MemoryAlgorithm.calculateNextReviewDate(
            reviewCount, blurCount, forgetCount, learningTimeMs, today
        )
        assertEquals(
            "reviewCount=$reviewCount blur=$blurCount forget=$forgetCount timeMs=$learningTimeMs",
            today.plusDays(expectedDays), result
        )
    }

    // ============ 1. 无困难：按基础间隔推进 1/2/4/7/14/21/30 ============

    @Test
    fun `无困难时按基础间隔推进`() {
        assertNextDate(0, 0, 0, 0, 1)
        assertNextDate(1, 0, 0, 0, 2)
        assertNextDate(2, 0, 0, 0, 4)
        assertNextDate(3, 0, 0, 0, 7)
        assertNextDate(4, 0, 0, 0, 14)
        assertNextDate(5, 0, 0, 0, 21)
        assertNextDate(6, 0, 0, 0, 30)
    }

    // ============ 2. 复习次数边界 ============

    @Test
    fun `复习次数为负数时按 0 处理`() {
        assertNextDate(-1, 0, 0, 0, 1)
    }

    @Test
    fun `复习次数大于等于 7 时间隔封顶 30 天`() {
        assertNextDate(7, 0, 0, 0, 30)
        assertNextDate(100, 0, 0, 0, 30)
    }

    // ============ 3. 困难程度缩短间隔 ============

    @Test
    fun `忘记一次会缩短间隔`() {
        // base=7, forget=1 → difficulty=0.6*0.5=0.3 → adjustment=0.76 → round(7*0.76)=5
        assertNextDate(3, 0, 1, 0, 5)
    }

    @Test
    fun `模糊一次会缩短间隔但弱于忘记`() {
        // base=7, blur=1 → difficulty=0.4*0.3=0.12 → adjustment=0.904 → round(7*0.904)=6
        assertNextDate(3, 1, 0, 0, 6)
    }

    @Test
    fun `忘记比模糊对间隔影响更大`() {
        // 同样 base=21：forget=1 → 16 天 < blur=1 → 19 天
        assertNextDate(5, 0, 1, 0, 16)
        assertNextDate(5, 1, 0, 0, 19)
    }

    @Test
    fun `学习时间越长困难度越高间隔越短`() {
        // blur=1, base=21：time 四档各差约 1 天（0/0.4/0.7/1.0）
        assertNextDate(5, 1, 0, 0, 19)          // time=0
        assertNextDate(5, 1, 0, 10_001, 18)     // 0.4
        assertNextDate(5, 1, 0, 30_001, 17)     // 0.7
        assertNextDate(5, 1, 0, 60_001, 16)     // 1.0
    }

    @Test
    fun `极难场景间隔收缩到最短 1 天`() {
        // forget>=3, blur>=3, time>60s → difficulty=1.0 → adjustment=0.2
        assertNextDate(0, 3, 3, 100_000, 1)     // base=1 → round(0.2)=0 → 钳制到 1
        assertNextDate(3, 3, 3, 100_000, 1)     // base=7 → round(1.4)=1
    }

    // ============ 4. 时间档位毫秒边界 ============

    @Test
    fun `学习时间 10 秒边界`() {
        // ≤10_000 → 0 分；>10_000 → 0.4 分
        assertNextDate(5, 1, 0, 10_000, 19)
        assertNextDate(5, 1, 0, 10_001, 18)
    }

    // ============ 5. 日期透传 ============

    @Test
    fun `返回日期为 reviewDate 加上间隔天数`() {
        val base = LocalDate.of(2024, 2, 29)   // 闰日，验证日期运算正确
        val result = MemoryAlgorithm.calculateNextReviewDate(3, 0, 0, 0, base)
        assertEquals(base.plusDays(7), result)
    }
}