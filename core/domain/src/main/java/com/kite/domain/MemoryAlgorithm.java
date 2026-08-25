package com.kite.domain;

import java.time.LocalDate;

public class MemoryAlgorithm {

    private static final int[] BASE_INTERVALS = {
            1, 2, 4, 7, 14, 21, 30
    };

    private static final int MIN_INTERVAL = 1;

    private static final int MAX_INTERVAL = 180;


    public static LocalDate calculateNextReviewDate(
            int reviewCount,
            int blurCount,
            int forgetCount,
            long learningTimeSeconds,
            LocalDate reviewDate
    ) {

        // 1. 获取当前复习阶段的基础间隔
        int baseInterval =
                getBaseInterval(reviewCount);


        // 2. 如果本次复习没有任何困难
        //    直接按照遗忘曲线的固定间隔
        if (blurCount == 0
                && forgetCount == 0) {

            return reviewDate.plusDays(
                    baseInterval
            );
        }


        // 3. 计算本次复习困难程度
        double difficulty =
                calculateDifficulty(
                        blurCount,
                        forgetCount,
                        learningTimeSeconds
                );


        // 4. 根据困难程度计算调整系数
        double adjustment =
                calculateAdjustment(
                        difficulty
                );


        // 5. 计算下一次间隔
        int nextInterval =
                (int) Math.round(
                        baseInterval
                                * adjustment
                );


        // 6. 限制最小/最大间隔
        nextInterval =
                Math.max(
                        MIN_INTERVAL,
                        Math.min(
                                MAX_INTERVAL,
                                nextInterval
                        )
                );


        return reviewDate.plusDays(
                nextInterval
        );
    }


    private static int getBaseInterval(
            int reviewCount
    ) {

        if (reviewCount < 0) {
            reviewCount = 0;
        }

        if (reviewCount
                < BASE_INTERVALS.length) {

            return BASE_INTERVALS[
                    reviewCount
                    ];
        }

        return 30;
    }


    private static double calculateDifficulty(
            int blurCount,
            int forgetCount,
            long learningTimeSeconds
    ) {

        // Forget 分数
        double forgetScore;

        if (forgetCount == 1) {
            forgetScore = 0.6;
        } else if (forgetCount == 2) {
            forgetScore = 0.9;
        } else if (forgetCount >= 3) {
            forgetScore = 1.0;
        } else {
            forgetScore = 0.0;
        }


        // Blur 分数
        double blurScore;

        if (blurCount == 1) {
            blurScore = 0.4;
        } else if (blurCount == 2) {
            blurScore = 0.7;
        } else if (blurCount >= 3) {
            blurScore = 1.0;
        } else {
            blurScore = 0.0;
        }


        // 学习时间分数
        double timeScore;

        if (learningTimeSeconds <= 10_000) {
            timeScore = 0.0;
        } else if (learningTimeSeconds <= 30_000) {
            timeScore = 0.4;
        } else if (learningTimeSeconds <= 60_000) {
            timeScore = 0.7;
        } else {
            timeScore = 1.0;
        }


        // 综合困难程度
        return
                forgetScore * 0.5
                        +
                        blurScore * 0.3
                        +
                        timeScore * 0.2;
    }


    private static double calculateAdjustment(
            double difficulty
    ) {

        // difficulty:
        // 0.0 → 正常
        // 1.0 → 极难

        // 调整范围：
        // 1.0 → 正常间隔
        // 0.2 → 最短间隔

        return 1.0 - difficulty * 0.8;
    }
}
