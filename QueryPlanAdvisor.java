package com.example.query;

import java.util.Comparator;
import java.util.List;

public final class QueryPlanAdvisor {
  public record TenantStats(long rowCount, double selectivity) {
    public TenantStats {
      if (rowCount < 1 || selectivity <= 0 || selectivity > 1) {
        throw new IllegalArgumentException("invalid tenant statistics");
      }
    }
  }

  public record Plan(String type, String detail, double estimatedCost) {}

  public record Recommendation(Plan selected, boolean regression, String reason) {}

  public Recommendation advise(TenantStats stats, List<String> candidateIndexes, Plan baseline) {
    Plan tableScan = new Plan("TABLE_SCAN", "full tenant partition", stats.rowCount());
    Plan best = candidateIndexes.stream()
        .map(index -> new Plan("INDEX_SCAN", index, indexCost(stats)))
        .min(Comparator.comparingDouble(Plan::estimatedCost))
        .filter(index -> index.estimatedCost() < tableScan.estimatedCost())
        .orElse(tableScan);

    boolean regression = baseline != null && best.estimatedCost() > baseline.estimatedCost() * 1.10;
    String reason = regression
        ? "estimated cost exceeds baseline by more than 10%; retain baseline and investigate telemetry"
        : "selected lowest estimated cost plan from current tenant statistics";
    return new Recommendation(best, regression, reason);
  }

  private double indexCost(TenantStats stats) {
    return Math.log(stats.rowCount()) / Math.log(2) + (stats.rowCount() * stats.selectivity() * 1.2);
  }
}
