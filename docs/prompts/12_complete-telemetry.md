/speckit-specify

- Rename the grafana dashboard "SDD Example - HTTP Service Metrics" to "SDD Example - Telemetry".
- Add database metrics to the telemetry dashboard (connection pool size, query duration, etc.)
- Add RabbitMQ metrics to the telemetry dashboard (queue size, message count, etc.)
- Add SMTP metrics to the telemetry dashboard (messages sent, failed, etc.)

---
then:
/speckit-plan
/speckit-tasks
/speckit-implement

When you're done, write a journal entry in docs/journal with your process to implement this feature: did you find any bugs? did you have to fix any issue found in static code analysis? did you need to add tests to meet minimum coverage?
