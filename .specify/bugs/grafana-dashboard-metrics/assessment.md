# Bug Assessment: Grafana HTTP Service Metrics Dashboard Unit and Legend Issues

- **Slug**: grafana-dashboard-metrics
- **Created**: 2026-11-14T08:10:10+01:00
- **Source**: pasted text
- **Verdict**: valid
- **Severity**: low

## Report (verbatim or summarized)

> Investigate two issues in the "SDD Example - HTTP ServiceMetrics" dashboard:
> 1. The Java Heap Memory Usage chart shows the value in Bytes. It should use human-friendly units (MB, GB)
> 2. The HTTP Response Latency chart shows the same legend "Latency (ms)" in every line, making it impossible to understand what each line represents.

## Symptom

1. The `JVM Heap Memory Usage` panel in the `SDD Example - HTTP Service Metrics` dashboard displays raw byte counts instead of scaled MB or GB units.
2. The `HTTP Response Latency` panel renders multiple series lines, and every line uses the identical static legend `"Latency (ms)"`.

## Reproduction

1. Start the local observability stack with Docker Compose (`docker compose up -d`).
2. Send HTTP requests to the application so Prometheus collects HTTP and JVM metrics.
3. Open Grafana at `http://localhost:3000` and select the `SDD Example - HTTP Service Metrics` dashboard (`uid: sdd-http-service`).
4. Inspect the `JVM Heap Memory Usage` stat panel and observe raw byte numbers.
5. Inspect the `HTTP Response Latency` timeseries panel and observe multiple lines all labeled `Latency (ms)`.

## Suspected Code Paths

- `config/grafana/dashboards/http-service.json:58-71` — Panel 4 (`JVM Heap Memory Usage`, type `stat`) defines no `fieldConfig.defaults.unit` property, so Grafana renders the raw value from `sum(jvm_memory_used_bytes{area="heap"})` in bytes.
- `config/grafana/dashboards/http-service.json:43-56` — Panel 3 (`HTTP Response Latency`, type `timeseries`) sets `"legendFormat": "Latency (ms)"` with no label placeholders. Its PromQL fallback `(rate(http_server_requests_milliseconds_sum[1m]) / rate(http_server_requests_milliseconds_count[1m]))` does not aggregate labels with `sum(...) by (...)`, so it emits one series per label combination (`method`, `uri`, `status`, and other labels), all sharing the static `"Latency (ms)"` legend.
- `specs/007-local-run/contracts/grafana-dashboard-spec.json:43-71` — The original contract specification also lacks `fieldConfig.defaults.unit` on Panel 4 and uses a static `legendFormat` on Panel 3.

## Root Cause Hypothesis

Both defects come from incomplete panel configuration in `config/grafana/dashboards/http-service.json`. Panel 4 omits `fieldConfig.defaults.unit`, which causes Grafana to display unformatted byte values from `jvm_memory_used_bytes`. Panel 3 uses a static `legendFormat` string (`"Latency (ms)"`) while its PromQL fallback expression preserves all metric labels (`method`, `uri`, `status`, etc.), producing multiple unaggregated series with identical legend text. Confidence: high.

## Proposed Remediation

**Preferred**:
1. In `config/grafana/dashboards/http-service.json`, add `"fieldConfig": { "defaults": { "unit": "decbytes" }, "overrides": [] }` (or `"unit": "bytes"` for IEC `MiB`/`GiB`) to Panel 4 (`JVM Heap Memory Usage`) so Grafana scales raw bytes automatically into human-friendly units (`MB`, `GB`).
2. In Panel 3 (`HTTP Response Latency`), aggregate the PromQL expression by the meaningful HTTP dimensions (`by (method, uri)` or `by (method, uri, status)`) and update `legendFormat` to `"{{method}} {{uri}}"` (matching Panel 1's convention) so each line has a distinct, readable label.

**Alternatives**:
- Aggregate Panel 3 into a single overall latency series (`sum(rate(http_server_requests_milliseconds_sum[1m])) / sum(rate(http_server_requests_milliseconds_count[1m]))`) if a single average line is preferred over per-endpoint lines. This hides per-endpoint latency differences.
- Keep `"unit": "bytes"` instead of `"decbytes"` on Panel 4 to use base-2 units (`MiB`, `GiB`), which is standard for JVM heap sizing.

**Files likely to change**:
- `config/grafana/dashboards/http-service.json`

**Tests to add or update**:
- No automated Grafana dashboard tests exist in `src/test`. Verify the updated JSON parses cleanly and displays the expected units and legends in the local Grafana container (`docker restart sdd-grafana`).
- Optionally add a lightweight JSON schema or unit test if dashboard validation is desired, though none is currently present.

## Risks & Considerations

- Changing Panel 3's `expr` to aggregate `by (method, uri)` (or `by (method, uri, status)`) changes the number of plotted lines; verify that `http_server_requests_milliseconds_bucket` (if present) also groups by the same labels so both branches of `or` match.
- `config/grafana/dashboards/http-service.json` is mounted read-only into `sdd-grafana` via `docker-compose.yml`; Grafana must reload provisioned dashboards (or restart) to pick up changes.

## Open Questions

- [NEEDS CLARIFICATION: For Panel 4, do you prefer SI units (`decbytes`: `MB`/`GB`) or IEC units (`bytes`: `MiB`/`GiB`)?]
- [NEEDS CLARIFICATION: For Panel 3, should each line represent `{{method}} {{uri}}` (like Panel 1), `{{method}} {{uri}} ({{status}})`, or a single aggregated line?]

