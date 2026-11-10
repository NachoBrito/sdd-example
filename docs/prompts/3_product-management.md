/speckit-specify

- Add support for the Product aggregate:

    - id (UUID, required, unique) Unique identifier
    - sku (String, required) Stock Keeping Unit
    - name (String, required) Human-readable product name
    - description (String, optional) Detailed description
    - priceInCents (Integer, required) Product price in integer cents
    - currency (String, required, ISO-4217) currency code
    - status (Enum, required) Lifecycle status: DRAFT, ACTIVE, ARCHIVED
    - createdAt (DateTime, required) Creation timestamp
    - updatedAt (DateTime, required) Last update timestamp

- Generate full REST API:
    - POST /products
    - GET /products/:id
    - PUT /products/:id
    - DELETE /products/:id
    - GET /products (filter by sku, name regex, status, sort by name, status, createdAt)

- We use client-side id generation.
    - Make the id a required parameter in the create product endpoint
    - Implement proper validation in every layer: controller, service and repository

- We express prices in cents:
    - Add proper validation in every layer: controller, service and repository

---
then:
/speckit-plan
/speckit-tasks
/speckit-implement

When you're done, write a journal entry in docs/journal with your process to implement this feature: did you find any bugs? did you have to fix any issue found in static code analysis? did you need to add tests to meet minimum coverage?