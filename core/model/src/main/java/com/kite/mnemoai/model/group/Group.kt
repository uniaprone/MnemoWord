package com.kite.mnemoai.model.group

data class Group(
    val id: Long,
    val name: String,
    val description: String,
    val createTime: Long,
    val isLearning: Int
)

data class GroupDetail(
    val group: Group,
    val totalWords: Int,
    val learningCount: Int,
    val reviewingCount: Int,
    val masteredCount: Int
)
