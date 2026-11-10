# Feature Specification: Product Management

**Feature Branch**: `003-product-management`

**Created**: 2026-11-10

**Status**: Draft

**Input**: User description: "Add support for the Product aggregate and full REST API with client-side id generation and price validation in cents."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create and Retrieve Product (Priority: P1)

A client application creates a new product with a client-generated unique identifier. The client application then retrieves the product details to verify the stored attributes.

**Why this priority**: The creation and retrieval of products forms the core capability of the product catalog. Without this capability, no other product operations can function.

**Independent Test**: The user can create a product with a valid identifier and immediately fetch the product by that identifier.

**Acceptance Scenarios**:

1. **Given** a valid client-generated UUID and valid product attributes, **When** the client submits a product creation request, **Then** the system stores the product and returns the complete product record.
2. **Given** an existing product identifier, **When** the client requests the product by that identifier, **Then** the system returns the stored product data.
3. **Given** a product creation request with a missing or invalid identifier, **When** the client submits the request, **Then** the system rejects the request with a validation error.
4. **Given** a product creation request with an identifier that already exists, **When** the client submits the request, **Then** the system rejects the request with a conflict error.
5. **Given** a product creation request with a duplicate SKU, **When** the client submits the request, **Then** the system rejects the request with a conflict error.

---

### User Story 2 - Search and Filter Products (Priority: P2)

A catalog manager searches and filters products using SKU, name patterns, and lifecycle status. The manager also sorts the results by name, status, or creation date.

**Why this priority**: Finding and sorting products enables catalog navigation and integration with downstream systems.

**Independent Test**: The user can search products using query filters and verify the returned items match the criteria and sort order.

**Acceptance Scenarios**:

1. **Given** multiple stored products, **When** the client filters by status, **Then** the system returns only products that match the specified status.
2. **Given** multiple stored products, **When** the client filters by a regular expression pattern on product name, **Then** the system returns matching products.
3. **Given** multiple stored products, **When** the client filters by SKU, **Then** the system returns matching products.
4. **Given** multiple stored products, **When** the client specifies a sort field and direction, **Then** the system returns products ordered by that field.

---

### User Story 3 - Update Product Information (Priority: P3)

A catalog manager updates the attributes of an existing product, such as the name, description, price, currency, or lifecycle status.

**Why this priority**: Product details change over time. Managers need to adjust prices, descriptions, and statuses throughout the product lifecycle.

**Independent Test**: The user can update attributes of an existing product and verify that subsequent reads reflect the changes.

**Acceptance Scenarios**:

1. **Given** an existing product, **When** the client sends updated product attributes, **Then** the system updates the product and modifies the update timestamp.
2. **Given** an update request with a negative price in cents, **When** the client sends the update, **Then** the system rejects the request with a validation error.
3. **Given** an update request for a nonexistent product identifier, **When** the client sends the update, **Then** the system returns a not found error.

---

### User Story 4 - Remove Product (Priority: P4)

A catalog manager permanently removes a product from the catalog by its identifier.

**Why this priority**: Products that are discontinued or created in error must be removable from the catalog.

**Independent Test**: The user can delete a product by identifier and verify that subsequent retrieval attempts return a not found error.

**Acceptance Scenarios**:

1. **Given** an existing product identifier, **When** the client requests deletion of the product, **Then** the system permanently removes the product record from the database.
2. **Given** a nonexistent product identifier, **When** the client requests deletion, **Then** the system returns a not found error.

---

### Edge Cases

- What happens when a client submits a negative value for price in cents? The system must reject the request with a validation error.
- What happens when a client submits zero for price in cents? The system must accept zero cents for free or promotional items.
- What happens when a client submits an invalid currency code? The system must validate the code against ISO-4217 and reject invalid values.
- What happens when a search filter matches zero products? The system must return an empty list without error.
- What happens when a regular expression in a name filter is malformed? The system must reject the query with a descriptive error.
- What happens when a client provides an invalid UUID string? The system must reject the request at the perimeter boundary.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST allow clients to create a product using a client-provided UUID identifier.
- **FR-002**: The system MUST require the following fields on product creation: id, sku, name, priceInCents, currency, and status.
- **FR-003**: The system MUST support an optional description text field for each product.
- **FR-004**: The system MUST validate that the product identifier is a valid and unique UUID.
- **FR-005**: The system MUST validate that the product price is a non-negative integer expressed in cents (greater than or equal to zero).
- **FR-006**: The system MUST validate that the currency code conforms to the three-letter ISO-4217 standard.
- **FR-007**: The system MUST enforce product lifecycle status values to be one of DRAFT, ACTIVE, or ARCHIVED.
- **FR-008**: The system MUST automatically record the creation timestamp and the last update timestamp.
- **FR-009**: The system MUST retrieve a single product record by its unique identifier.
- **FR-010**: The system MUST update product attributes by identifier and refresh the last update timestamp.
- **FR-011**: The system MUST permanently remove the product record from the database when handling a deletion request by identifier.
- **FR-012**: The system MUST list products with filtering by SKU, name regular expression pattern, and status.
- **FR-013**: The system MUST support sorting product listings by name, status, or creation timestamp.
- **FR-014**: The system MUST enforce that the SKU is unique across all products in the catalog.
- **FR-015**: The system MUST enforce contract validation at all system boundaries and layers.

### Key Entities *(include if feature involves data)*

- **Product**:
  - `id`: Unique identifier, required, format UUID, supplied by the client.
  - `sku`: Stock Keeping Unit, required, unique across the catalog, non-empty text identifier.
  - `name`: Human-readable product name, required, non-empty text.
  - `description`: Detailed product description, optional text.
  - `priceInCents`: Product price, required, non-negative integer representing monetary value in cents (zero or positive).
  - `currency`: Currency code, required, uppercase three-letter ISO-4217 code.
  - `status`: Lifecycle status, required, one of DRAFT, ACTIVE, ARCHIVED.
  - `createdAt`: Date and time of product creation, required, set by system.
  - `updatedAt`: Date and time of last update, required, maintained by system.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of product creation requests with valid client-generated UUIDs are accepted and persisted.
- **SC-002**: 100% of invalid payloads (malformed UUIDs, negative prices, invalid currency codes) are rejected at the boundary with clear error details.
- **SC-003**: Clients can retrieve any persisted product by its identifier in a single request.
- **SC-004**: Search queries accurately filter products across all supported filter criteria (SKU, name pattern, status).
- **SC-005**: Search results accurately reflect requested sort orders (name, status, creation timestamp).
- **SC-006**: All layers strictly enforce schema validation for prices, identifiers, and unique constraints.

## Assumptions

- Product identifiers are generated as UUID version 4 by the client before submission.
- The description attribute is optional and may be empty or omitted.
- The default sort direction is ascending when not specified by the client.
- Empty search filter parameters indicate no filtering on that attribute.
- Timestamps are captured and returned in UTC format.
- Free or promotional products with a price of zero cents are permitted.
