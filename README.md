# Multi-Tenant Query Plan Advisor

A small Java prototype for cost-aware query plan selection in a multi-tenant SaaS database.

## What it models

- Tenant-specific row counts and column selectivity.
- Cost comparison between table scans and candidate index scans.
- Regression detection against a baseline query plan.
- Explainable recommendations for a diagnostic service.

## Run

Compile source and tests with Java 21, then run with assertions enabled.

## Design notes

The cost model estimates scan cost from tenant row count and index cost from selectivity plus a logarithmic lookup penalty. A production service would add database statistics, execution telemetry, plan-cache history, and controlled rollouts.
