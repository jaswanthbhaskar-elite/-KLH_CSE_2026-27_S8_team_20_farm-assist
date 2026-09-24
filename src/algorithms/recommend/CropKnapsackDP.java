package algorithms.recommend;

import models.Crop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Multi-dimensional 0/1 Knapsack DP for crop recommendation.
 *
 * State: dp[i][l][b][w] = max profit using first i crops,
 *        with land used <= l, budget used <= b, water used <= w.
 *
 * Budget is scaled into units of Rs.1000 to keep the DP table small.
 * Crop cost is rounded UP when scaling (never underestimate cost).
 * User's available budget is rounded DOWN when scaling (never overestimate capacity).
 *
 * Time complexity : O(n * L * B' * W)   B' = scaled budget capacity
 * Space complexity: O(n * L * B' * W)   full table kept for backtracking
 */
public class CropKnapsackDP {

    private static final int BUDGET_SCALE = 1000;

    public static class Result {
        public final List<Crop> selectedCrops;
        public final int totalLand;
        public final int totalBudget;
        public final int totalWater;
        public final int totalProfit;

        public Result(List<Crop> selectedCrops, int totalLand, int totalBudget,
                       int totalWater, int totalProfit) {
            this.selectedCrops = selectedCrops;
            this.totalLand = totalLand;
            this.totalBudget = totalBudget;
            this.totalWater = totalWater;
            this.totalProfit = totalProfit;
        }
    }

    public static Result recommend(List<Crop> crops, int landCapacity,
                                    int budgetCapacity, int waterCapacity) {
        int n = crops.size();

        // Scale budget capacity DOWN (floor) so we never assume more budget than available.
        int scaledBudgetCapacity = budgetCapacity / BUDGET_SCALE;

        int[] land = new int[n];
        int[] budget = new int[n]; // scaled
        int[] water = new int[n];
        int[] profit = new int[n];

        for (int i = 0; i < n; i++) {
            Crop c = crops.get(i);
            land[i] = c.getLandRequired();
            // Scale crop cost UP (ceiling) so we never underestimate what it truly costs.
            budget[i] = (c.getBudgetRequired() + BUDGET_SCALE - 1) / BUDGET_SCALE;
            water[i] = c.getWaterUnits();
            profit[i] = c.getExpectedProfit();
        }

        int[][][][] dp = new int[n + 1][landCapacity + 1][scaledBudgetCapacity + 1][waterCapacity + 1];

        for (int i = 1; i <= n; i++) {
            int cl = land[i - 1], cb = budget[i - 1], cw = water[i - 1], cp = profit[i - 1];
            for (int l = 0; l <= landCapacity; l++) {
                for (int b = 0; b <= scaledBudgetCapacity; b++) {
                    for (int w = 0; w <= waterCapacity; w++) {
                        int best = dp[i - 1][l][b][w]; // exclude crop i
                        if (cl <= l && cb <= b && cw <= w) {
                            int candidate = dp[i - 1][l - cl][b - cb][w - cw] + cp; // include crop i
                            if (candidate > best) best = candidate;
                        }
                        dp[i][l][b][w] = best;
                    }
                }
            }
        }

        // Backtrack to find which crops were actually selected.
        List<Crop> selected = new ArrayList<>();
        int l = landCapacity, b = scaledBudgetCapacity, w = waterCapacity;
        for (int i = n; i >= 1; i--) {
            if (dp[i][l][b][w] != dp[i - 1][l][b][w]) {
                Crop chosen = crops.get(i - 1);
                selected.add(chosen);
                l -= land[i - 1];
                b -= budget[i - 1];
                w -= water[i - 1];
            }
        }
        Collections.reverse(selected);

        int totalLand = 0, totalBudget = 0, totalWater = 0;
        for (Crop c : selected) {
            totalLand += c.getLandRequired();
            totalBudget += c.getBudgetRequired();
            totalWater += c.getWaterUnits();
        }
        int totalProfit = dp[n][landCapacity][scaledBudgetCapacity][waterCapacity];

        return new Result(selected, totalLand, totalBudget, totalWater, totalProfit);
    }
}