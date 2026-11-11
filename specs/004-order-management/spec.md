# Feature Specification: Order Management

**Feature Branch**: `004-order-management`

**Created**: 2026-11-10

**Status**: Draft

**Input**: User description: "Add support for the Order aggregate: - Entities: - order (id, customerName, customerEmail, date, status (PENDING, PAID, SHIPPED, DELIVERED, CANCELLED), totalAmountInCents, currency) - deliveryAddress (name, telephone, street,complement, city, state, zipCode, country) - invoiceAddress (name, vatNumber, telephone, street,complement, city, state, zipCode, country) - orderLine (productId, quantity, unitPriceInCents, lineTotalInCents) - Generate full REST API: - POST /orders - GET /orders/:id - PUT /orders/:id - DELETE /orders/:id - GET /orders (filter by status, customerEmail, dateRange (startDate, endDate), sort by date, status)"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create and Retrieve Order (Priority: P1)

A client application creates a new order with addresses and order lines. The client application then retrieves the order details to verify the stored attributes.

**Why this priority**: The creation and retrieval of orders form the core capability of the order management system. Without this capability, no other order operations can function.

**Independent Test**: The user can create an order with valid lines and addresses and immediately fetch the order by its identifier.

**Acceptance Scenarios**:

1. **Given** valid customer details, addresses, order lines, and a client-generated UUID, **When** the client submits an order creation request, **Then** the system stores the order and returns the complete order record.
2. **Given** an existing order identifier, **When** the client requests the order by that identifier, **Then** the system returns the stored order record.
3. **Given** an order creation request with an invalid email address or missing customer name, **When** the client submits the request, **Then** the system rejects the request with a validation error.
4. **Given** an order creation request with zero order lines, **When** the client submits the request, **Then** the system rejects the request with a validation error.
5. **Given** an order creation request with an existing order identifier, **When** the client submits the request, **Then** the system rejects the request with a conflict error.
6. **Given** an order creation request with a missing or invalid UUID identifier, **When** the client submits the request, **Then** the system rejects the request with a validation error.

---

### User Story 2 - Search and Filter Orders (Priority: P2)

A sales manager searches and filters orders using status, customer email, and date ranges. The manager also sorts the results by date or status.

**Why this priority**: Finding and sorting orders enables order tracking, customer support, and operational fulfillment.

**Independent Test**: The user can query orders using status, email, and date range filters, and verify that the system returns matching orders in the requested sort order.

**Acceptance Scenarios**:

1. **Given** multiple stored orders, **When** the client filters orders by status, **Then** the system returns only orders that match the specified status.
2. **Given** multiple stored orders, **When** the client filters orders by customer email, **Then** the system returns only orders that match that customer email.
3. **Given** multiple stored orders, **When** the client filters orders by date range, **Then** the system returns only orders created within that range.
4. **Given** multiple stored orders, **When** the client specifies sort by date or status, **Then** the system returns orders ordered by that field.

---

### User Story 3 - Update Order (Priority: P3)

A sales manager updates the details of an existing order.

**Why this priority**: Order details, addresses, lines, and lifecycle statuses change during processing.

**Independent Test**: The user can update an existing order and verify that subsequent reads reflect the updated data.

**Acceptance Scenarios**:

1. **Given** an existing order, **When** the client submits updated order details, **Then** the system updates the order record.
2. **Given** an update request for a nonexistent order identifier, **When** the client submits the update, **Then** the system returns a not found error.
3. **Given** an update request with an invalid line quantity or invalid address, **When** the client submits the update, **Then** the system rejects the request with a validation error.

---

### User Story 4 - Remove Order (Priority: P4)

A sales manager removes an order from the system by its identifier.

**Why this priority**: Orders created in error or cancelled must be removable according to retention policies.

**Independent Test**: The user can delete an order by identifier and verify that subsequent retrieval attempts return a not found error.

**Acceptance Scenarios**:

1. **Given** an existing order with status PENDING or CANCELLED, **When** the client requests deletion of the order, **Then** the system permanently removes the order from the database.
2. **Given** an existing order with status PAID, SHIPPED, or DELIVERED, **When** the client requests deletion of the order, **Then** the system rejects the request with a conflict error.
3. **Given** a nonexistent order identifier, **When** the client requests deletion, **Then** the system returns a not found error.

---

### Edge Cases

- What happens when an order line has a quantity of zero or negative? The system rejects the request with a validation error.
- What happens when a unit price in cents is negative? The system rejects the request with a validation error.
- What happens when a date range filter specifies a start date after the end date? The system rejects the query with a validation error.
- What happens when search filters match zero orders? The system returns an empty list without error.
- What happens when a client submits an invalid currency code? The system validates the code against ISO-4217 and rejects invalid values.
- What happens when a client provides an invalid UUID string? The system rejects the request at the perimeter boundary.
- What happens when a client requests deletion of an order with status PAID, SHIPPED, or DELIVERED? The system rejects the request with a conflict error.
- What happens when a client submits mismatched line totals or order totals? The server calculates and enforces the correct totals from line quantities and unit prices.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST allow clients to create an order with customer details, delivery address, invoice address, and order lines.
- **FR-002**: The system MUST require clients to provide a unique UUID as the order identifier on order creation.
- **FR-003**: The system MUST require customerName and customerEmail on order creation.
- **FR-004**: The system MUST validate that customerEmail is a syntactically valid email address.
- **FR-005**: The system MUST require a deliveryAddress with name, telephone, street, city, state, zipCode, and country.
- **FR-006**: The system MUST require an invoiceAddress with name, vatNumber, telephone, street, city, state, zipCode, and country.
- **FR-007**: The system MUST support an optional complement field on deliveryAddress and invoiceAddress.
- **FR-008**: The system MUST require at least one orderLine for each order.
- **FR-009**: The system MUST require productId, quantity, and unitPriceInCents on each orderLine.
- **FR-010**: The system MUST validate that orderLine quantity is an integer greater than zero.
- **FR-011**: The system MUST validate that orderLine unitPriceInCents is an integer greater than or equal to zero.
- **FR-012**: The system MUST calculate lineTotalInCents as the product of quantity and unitPriceInCents for each orderLine.
- **FR-013**: The system MUST calculate totalAmountInCents as the sum of all lineTotalInCents values for the order.
- **FR-014**: The system MUST enforce order status to be one of PENDING, PAID, SHIPPED, DELIVERED, or CANCELLED.
- **FR-015**: The system MUST set initial order status to PENDING upon creation when status is omitted.
- **FR-016**: The system MUST validate that currency conforms to the three-letter ISO-4217 standard.
- **FR-017**: The system MUST record the creation date and time for each order.
- **FR-018**: The system MUST retrieve an order by its unique identifier.
- **FR-019**: The system MUST update order details by identifier.
- **FR-020**: The system MUST permanently remove an order from the database when its status is PENDING or CANCELLED upon receiving a deletion request.
- **FR-021**: The system MUST reject deletion of an order with a conflict error when its status is PAID, SHIPPED, or DELIVERED.
- **FR-022**: The system MUST list orders with filtering by status, customerEmail, and date range (startDate to endDate).
- **FR-023**: The system MUST sort order listings by date or status.

### Key Entities *(include if feature involves data)*

- **Order**:
  - `id`: Unique identifier, required, format UUID, supplied by the client.
  - `customerName`: Name of the customer, required, non-empty text.
  - `customerEmail`: Email address of the customer, required, valid email format.
  - `date`: Timestamp of the order, required.
  - `status`: Lifecycle status, required, one of PENDING, PAID, SHIPPED, DELIVERED, CANCELLED.
  - `totalAmountInCents`: Monetary total of the order in cents, non-negative integer, calculated by system.
  - `currency`: Currency code, required, uppercase three-letter ISO-4217 code.
  - `deliveryAddress`: Delivery address entity, required.
  - `invoiceAddress`: Invoice address entity, required.
  - `orderLines`: List of one or more order line entities, required.

- **DeliveryAddress**:
  - `name`: Recipient person or company name, required, non-empty text.
  - `telephone`: Contact telephone number, required, non-empty text.
  - `street`: Street address, required, non-empty text.
  - `complement`: Secondary address details, optional text.
  - `city`: City name, required, non-empty text.
  - `state`: State or province name, required, non-empty text.
  - `zipCode`: Postal code, required, non-empty text.
  - `country`: Country name or code, required, non-empty text.

- **InvoiceAddress**:
  - `name`: Invoiced person or company name, required, non-empty text.
  - `vatNumber`: Tax or VAT identification number, required, non-empty text.
  - `telephone`: Contact telephone number, required, non-empty text.
  - `street`: Street address, required, non-empty text.
  - `complement`: Secondary address details, optional text.
  - `city`: City name, required, non-empty text.
  - `state`: State or province name, required, non-empty text.
  - `zipCode`: Postal code, required, non-empty text.
  - `country`: Country name or code, required, non-empty text.

- **OrderLine**:
  - `productId`: Unique identifier of the product, required, format UUID.
  - `quantity`: Quantity of items, required, integer greater than zero.
  - `unitPriceInCents`: Price per unit in cents, required, non-negative integer.
  - `lineTotalInCents`: Total price for the line in cents, required, non-negative integer, calculated by system.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of order creation requests with valid data and client-generated UUIDs are accepted and stored.
- **SC-002**: 100% of invalid payloads are rejected at the perimeter boundary with clear validation messages.
- **SC-003**: Clients can retrieve any stored order by its identifier in a single request.
- **SC-004**: Search queries accurately filter orders across status, customer email, and date ranges.
- **SC-005**: Search results accurately reflect requested sort orders for date and status.
- **SC-006**: 100% of stored order line totals and order totals maintain mathematical consistency.

## Assumptions

- The client supplies a client-generated UUID on order creation.
- The server calculates lineTotalInCents and totalAmountInCents, enforcing data integrity.
- Hard deletion is restricted to orders in PENDING or CANCELLED status; fulfilled or paid orders cannot be deleted.
- The default sort order direction is ascending when not specified by the client.
- Empty query filter parameters indicate no filtering on that attribute.
- Timestamps are captured and returned in UTC format.
- Addresses require standard geographic fields; complement remains optional.
- The default initial status for a newly created order is PENDING.
