/speckit-specify


Add support for the Order aggregate:

- Entities:
    - order (id, customerName, customerEmail, date, status (PENDING, PAID, SHIPPED, DELIVERED, CANCELLED), totalAmountInCents, currency)
    - deliveryAddress (name, telephone, street,complement, city, state, zipCode, country)
    - invoiceAddress (name, vatNumber, telephone, street,complement, city, state, zipCode, country)
    - orderLine (productId, quantity, unitPriceInCents, lineTotalInCents)

- Generate full REST API:
    - POST /orders 
    - GET /orders/:id
    - PUT /orders/:id
    - DELETE /orders/:id
    - GET /orders (filter by status, customerEmail, dateRange (startDate, endDate), sort by date, status)

---
then:
/speckit-plan
/speckit-tasks
/speckit-implement

When you're done, write a journal entry in docs/journal with your process to implement this feature: did you find any bugs? did you have to fix any issue found in static code analysis? did you need to add tests to meet minimum coverage?
