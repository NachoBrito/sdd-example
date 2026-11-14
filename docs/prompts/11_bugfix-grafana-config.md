/speckit-bug-assess

Investigate two issues in the "SDD Example - HTTP ServiceMetrics" dashboard:
1. The Java Heap Memory Usage chart shows the value in Bytes. It should use human-friendly units (MB, GB)
2. The HTTP Response Latency chart shows the same legend "Latency (ms)" in every line, making it impossible to understand what each line represents.


---
then:

/speckit-bug-fix slug=grafana-dashboard-metrics
