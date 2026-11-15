/speckit-specify

# 1. Introduce the Category entity (id (UUID), name(String, required), description(String, optional)), add REST endpoints to:
- Create a new category with a name and description (just for ADMIN users).
- List all categories, with optional filtering by name and description (same access as read product operations).
- Delete a category (just for ADMIN users).

Modify the Product entity to include a categoryId, and add validation to ensure that only valid categories are assigned to products (use the list endpoint to validate).

A category cannot be deleted if there are products in that category.

# 2. Introduce the Campaign entity:
- id(UUID)
- name(String, required)
- description(String, optional)
- status(enum, required, allowed values: DRAFT, ACTIVE, INACTIVE)
- startDate(DateTime, required)
- endDate(DateTime, required)
- discountPercent(Integer, required, values between 0 and 100)
- predicates(Predicate[])
    - predicateType(enum, required, allowed values: IN_CATEGORY, NOT_IN_CATEGORY, IS_CHEAPER_THAN, IS_MORE_EXPENSIVE_THAN)
    - value(String, required)

Add REST endpoints to manage campaigns:
- Create a new campaign with a name, description, status and start and end dates (just for ADMIN users).
- Update a campaign (just for ADMIN users).
- List all campaigns, with optional filtering by name, description, status and start and end dates (just for ADMIN users).
- Delete a campaign (just for ADMIN users).

Modify the list products and get product endpoints to include the active discounts that apply to the product. Note we only change the HTTP resource, not the backend domain entities. The active campaigns are resolved at request time.

---
then:
/speckit-plan
/speckit-tasks
/speckit-implement

When you're done, update the running/requests files, and write a journal entry in docs/journal with your process to implement this feature: did you find any bugs? did you have to fix any issue found in static code analysis? did you need to add tests to meet minimum coverage?

---
then:

Add a new technical design document to the docs folder describing this service:

- Document the domain model, with a mermaid entity-relation diagram
- Add a component diagram
- Document the main use cases with flow diagrams

Link this document from the README