package com.kite.mnemoai.utils;

import java.time.LocalDate;

public class MemoryAlgorithm {
    public static String calculateNextReviewDate(int reviewTimes, LocalDate date){
        String nextReviewDate;
        switch (reviewTimes){
            case 0:
                nextReviewDate = date.plusDays(1).toString();
                break;
            case 1:
                nextReviewDate = date.plusDays(2).toString();
                break;
            case 2:
                nextReviewDate = date.plusDays(4).toString();
                break;
            case 3:
                nextReviewDate = date.plusDays(7).toString();
                break;
            case 4:
                nextReviewDate = date.plusDays(14).toString();
                break;
            case 5:
                nextReviewDate = date.plusDays(21).toString();
                break;
            default:
                nextReviewDate = date.plusDays(30).toString();
                break;
        }
        return nextReviewDate;
    }
}
