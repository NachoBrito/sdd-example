/speckit-specify


Add asyncrhronous notifications:

1. Create a notification service that accepts multiple transports, SMTP initially.
2. When an order is created, the notification service sends the customer the order details.
3. Each transport uses its own template. The SMTP transport will have an html template.
4. We want this to be fully asyncrhronous using RabbitMQ:
    - The notification service should publish events to RabbitMQ when an order is created, updated or cancelled.
    - The events should be consumed by a new RabbitMQ consumer that will send the notifications.
    - The consumer is part of the same notification service.
5. Besides unit testsing, cover this with integration tests using Micronaut test resources.

---
then:
/speckit-plan
/speckit-tasks
/speckit-implement

When you're done, write a journal entry in docs/journal with your process to implement this feature: did you find any bugs? did you have to fix any issue found in static code analysis? did you need to add tests to meet minimum coverage?
