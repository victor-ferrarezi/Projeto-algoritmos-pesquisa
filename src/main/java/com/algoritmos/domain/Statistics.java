package com.algoritmos.domain;

import java.util.Arrays;

public final class Statistics {
    private Statistics() {}
    public static double mean(double[] values) {
        if (values.length == 0) return Double.NaN;
        double sum = 0;
        for (double value : values) sum += value;
        return sum / values.length;
    }
    public static double median(double[] values) {
        if (values.length == 0) return Double.NaN;
        double[] copy = values.clone();
        Arrays.sort(copy);
        int n = copy.length;
        return n % 2 == 1 ? copy[n / 2] : (copy[n / 2 - 1] + copy[n / 2]) / 2.0;
    }
    public static double deviation(double[] values) {
        if (values.length < 2) return 0;
        double avg = mean(values), sum = 0;
        for (double value : values) sum += (value - avg) * (value - avg);
        return Math.sqrt(sum / (values.length - 1));
    }
}
