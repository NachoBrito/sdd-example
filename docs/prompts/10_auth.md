/speckit-specify

Define the User domain entity with the following attributes:

- userId (UUID, client generated)
- username (required, unique)
- password (required, hashed)
- roles (required, list of strings)

Add userId to orders, as a required field (this change will not be backwards compatible, so update any existing orders with userId=1).

Introduce JWT based authentication:

- Add endpoints to generate JWT token from user credentials (login)
- Add endpoints to validate JWT token
- Add endpoints to refresh JWT token
- Add endpoints to revoke JWT token
- Add endpoints to register new users

Add authorization rules:

- Initial roles are ADMIN, CUSTOMER, CATALOG
- Each user has one or more roles assigned. CUSTOMER is the default role for new users.
- Any user can access the login endpoint
- Any CUSTOMER user can list products, but only users with CATALOG role can see draft or archived products.
- Any CUSTOMER user can read an active product, only users with CATALOG role can see draft or archived products.
- Only users with CATALOG role can create or update products.
- Any CUSTOMER user can create orders
- Only ADMIN users can modify orders
- CUSTOMER user only see their own orders
- ADMIN users can list or read any order

--- then
[speckit-clarify](slashCommand;speckit-clarify) this change involves also updating the running/request scripts, the load testing infra and all the related documentation

---
then:
/speckit-plan
/speckit-tasks
/speckit-implement

When you're done, write a journal entry in docs/journal with your process to implement this feature: did you find any bugs? did you have to fix any issue found in static code analysis? did you need to add tests to meet minimum coverage?

[speckit-clarify](slashCommand;speckit-clarify) you have injected authorization logic in the application layer. Propose an alternate design where the authorization is implemented as a cross cutting concern using micronaut's aop capabilities. Separate interface from implementation in the authentication service so it can be easily mocked in tests or replaced by alternate implementations.


then:
/speckit-plan
/speckit-tasks
/speckit-implement

when you're done, update the journal entry with this design change (do not remote the previous approach, document all the discussion)