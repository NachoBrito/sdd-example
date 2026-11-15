# Feature Specification: Product Categories and Discount Campaigns

**Feature Branch**: `012-categories-campaigns`

**Created**: 2026-11-14

**Status**: Draft

**Input**: User description: "1. Introduce the Category entity (id (UUID), name(String, required), description(String, optional)), add REST endpoints to: Create a new category with a name and description (just for ADMIN users); List all categories, with optional filtering by name and description (same access as read product operations); Delete a category (just for ADMIN users). Modify the Product entity to include a categoryId, and add validation to ensure that only valid categories are assigned to products (use the list endpoint to validate). A category cannot be deleted if there are products in that category. 2. Introduce the Campaign entity (id, name, description, status DRAFT/ACTIVE/INACTIVE, startDate, endDate, discountPercent 0-100, predicates with predicateType IN_CATEGORY/NOT_IN_CATEGORY/IS_CHEAPER_THAN/IS_MORE_EXPENSIVE_THAN and value), add REST endpoints to manage campaigns (create, update, list with filters, delete - all just for ADMIN users). Modify the list products and get product operations to include the active discounts that apply to the product, changing only the external product resource and resolving active campaigns at request time."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Manage Product Categories (Priority: P1)

An administrator creates and deletes product categories, and authorized catalog readers list categories with optional name and description filters.

**Why this priority**: Categories establish the catalog taxonomy that products reference and that promotional campaigns target.

**Independent Test**: An administrator creates a category, an authorized catalog reader lists and filters categories to find it, and an administrator deletes an unused category.

**Acceptance Scenarios**:

1. **Given** an authenticated user with the `ADMIN` role and valid category details, **When** the administrator creates a category, **Then** the system stores and returns the category record.
2. **Given** an authenticated user without the `ADMIN` role, **When** the user submits a category creation or deletion request, **Then** the system rejects the request.
3. **Given** an authenticated user with the `CUSTOMER` or `CATALOG` role, **When** the user lists categories with optional `name` and `description` filters, **Then** the system returns all matching categories.
4. **Given** an unauthenticated caller or a user without `CUSTOMER` or `CATALOG` access, **When** the caller requests the category list, **Then** the system rejects the request.
5. **Given** an existing category with no assigned products, **When** an `ADMIN` user deletes that category, **Then** the system permanently removes the category.
6. **Given** an existing category that has one or more assigned products, **When** an `ADMIN` user requests deletion of that category, **Then** the system rejects the request with a conflict error.

---

### User Story 2 - Assign Valid Categories to Products (Priority: P1)

A catalog manager assigns a category identifier to a product during creation or update, and the system verifies that the referenced category exists.

**Why this priority**: Valid category assignments maintain catalog integrity, protect against broken references, and support category-based campaign rules.

**Independent Test**: Create a category, assign its identifier when creating and updating a product, and verify that an unknown category identifier is rejected.

**Acceptance Scenarios**:

1. **Given** an existing category identifier, **When** a `CATALOG` user creates or updates a product with that `categoryId`, **Then** the system persists and returns the product with that category association.
2. **Given** a category identifier that does not match any existing category, **When** a `CATALOG` user creates or updates a product with that `categoryId`, **Then** the system rejects the request with a validation error.
3. **Given** a product creation or update request that omits `categoryId`, **When** a `CATALOG` user submits it through a v1 product operation, **Then** the system accepts the uncategorized product; **When** submitted through a v2 product operation, **Then** the system rejects it with a validation error.

---

### User Story 3 - Manage Promotional Campaigns (Priority: P2)

An administrator creates, updates, lists, filters, and deletes promotional discount campaigns with schedules, percentages, statuses, and targeting predicates.

**Why this priority**: Campaign administration lets operators define discount rules and active date windows before discounts appear on product reads.

**Independent Test**: An administrator creates a campaign with predicates, updates its schedule and discount, lists campaigns using filters, and deletes the campaign.

**Acceptance Scenarios**:

1. **Given** an authenticated user with the `ADMIN` role and valid campaign attributes, **When** the administrator creates a campaign, **Then** the system stores and returns the campaign record.
2. **Given** an existing campaign identifier, **When** an `ADMIN` user updates the campaign attributes, **Then** the system validates and saves the updated campaign.
3. **Given** multiple stored campaigns, **When** an `ADMIN` user lists campaigns with optional filters for `name`, `description`, `status`, `startDate`, and `endDate`, **Then** the system returns matching campaigns.
4. **Given** an existing campaign identifier, **When** an `ADMIN` user deletes the campaign, **Then** the system permanently removes the campaign.
5. **Given** an authenticated user without the `ADMIN` role, **When** the user tries to create, update, list, or delete campaigns, **Then** the system rejects the request.
6. **Given** a campaign payload with `discountPercent` outside 0 to 100 or `endDate` earlier than `startDate`, **When** an `ADMIN` user submits the request, **Then** the system rejects it with a validation error.

---

### User Story 4 - Expose Active Discounts on Product Reads (Priority: P2)

When an authorized user retrieves a single product or lists products, the system evaluates active campaigns at request time and includes matching discounts in the product output.

**Why this priority**: Request-time discount resolution gives shoppers and catalog readers current promotional discounts without coupling campaign state into the core product model.

**Independent Test**: Configure an `ACTIVE` campaign whose date window includes the current time and whose predicates match a product, then fetch and list products to confirm the discount appears.

**Acceptance Scenarios**:

1. **Given** an `ACTIVE` campaign within its `startDate` and `endDate` window whose predicates match a product, **When** an authorized user retrieves or lists that product, **Then** the returned product output includes the active discount while the core product entity remains unchanged.
2. **Given** a campaign in `DRAFT` or `INACTIVE` status, or outside its active date window, **When** an authorized user retrieves or lists products, **Then** the system excludes that campaign from the product's active discounts.
3. **Given** multiple active campaigns that match the same product simultaneously, **When** an authorized user retrieves or lists that product, **Then** the product output lists all matching active discounts while keeping the catalog price unchanged.

---

### Edge Cases

- What happens when an `ADMIN` user deletes a nonexistent category or campaign? The system returns a not found error.
- What happens when an `ADMIN` user updates a nonexistent campaign? The system returns a not found error.
- What happens when category or campaign list filters match zero records? The system returns an empty list without error.
- What happens when a campaign list query specifies a `startDate` after `endDate`? The system rejects the query with a validation error.
- What happens when a category deletion is attempted while a `DRAFT` or `ARCHIVED` product still references that category? The system rejects the deletion because any product in the category prevents deletion.
- What happens when a campaign uses `discountPercent` equal to `0` or `100`? The system accepts both boundary values.
- What happens when the current request timestamp equals `startDate` or `endDate` of an `ACTIVE` campaign? The campaign is treated as active within the inclusive `[startDate, endDate]` interval.
- What happens when a campaign predicate supplies a malformed UUID for category predicates or a non-integer or negative value for price predicates? The system rejects the campaign with a validation error.
- What happens when an unauthenticated user accesses any category, campaign, or product operation? The system rejects the request and requires authentication.
- What happens when an uncategorized product (`categoryId` absent) is evaluated against category predicates? The product matches `NOT_IN_CATEGORY` and does not match `IN_CATEGORY`.
- What happens when a product price equals the threshold in `IS_CHEAPER_THAN` or `IS_MORE_EXPENSIVE_THAN`? The predicate does not match because price comparisons are strict (`<` and `>`).
- What happens when an active campaign has an empty `predicates` list? The campaign matches every product.
- What happens when no active campaigns match a product? The product output returns an empty list of active discounts.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST define a `Category` entity with a unique UUID `id`, a required non-blank `name`, and an optional `description`.
- **FR-002**: The system MUST allow only users with the `ADMIN` role to create a category with `name` and optional `description`.
- **FR-003**: The system MUST allow users permitted to read products (`CUSTOMER` or `CATALOG` roles) to list categories with optional filtering by `name` and `description`.
- **FR-004**: The system MUST allow only users with the `ADMIN` role to delete a category by identifier.
- **FR-005**: The system MUST reject deletion of a category with a conflict error when any product references that category.
- **FR-006**: The system MUST include a `categoryId` (UUID) attribute on the `Product` entity, keeping `categoryId` optional (nullable) for v1 product creation and update operations and existing stored products, and requiring a non-null `categoryId` for v2 product creation and update operations.
- **FR-007**: The system MUST validate on product creation and product update that any assigned `categoryId` exists in the category catalog, using the category list capability to verify valid categories, and MUST reject nonexistent category identifiers with a validation error.
- **FR-008**: The system MUST define a `Campaign` entity with UUID `id`, required `name`, optional `description`, required `status` (`DRAFT`, `ACTIVE`, `INACTIVE`), required `startDate` (DateTime), required `endDate` (DateTime), required `discountPercent` (integer from 0 to 100 inclusive), and `predicates` (`predicateType` in `IN_CATEGORY`, `NOT_IN_CATEGORY`, `IS_CHEAPER_THAN`, `IS_MORE_EXPENSIVE_THAN`, and required text `value`).
- **FR-009**: The system MUST validate that `discountPercent` is an integer between 0 and 100 inclusive and that `endDate` is not earlier than `startDate`.
- **FR-010**: The system MUST validate each campaign predicate so `predicateType` is one of the allowed values, `value` is non-blank, category predicates hold valid category UUID values, and price predicates hold valid non-negative integer amounts.
- **FR-011**: The system MUST allow only users with the `ADMIN` role to create campaigns.
- **FR-012**: The system MUST allow only users with the `ADMIN` role to update existing campaigns by identifier.
- **FR-013**: The system MUST allow only users with the `ADMIN` role to list campaigns with optional filtering by `name`, `description`, `status`, `startDate`, and `endDate`.
- **FR-014**: The system MUST allow only users with the `ADMIN` role to delete campaigns by identifier.
- **FR-015**: The system MUST evaluate campaign predicates against each product at request time using logical `AND` across all predicates, treating an empty `predicates` list as matching all products, matching uncategorized products (`categoryId` absent) only for `NOT_IN_CATEGORY` (never `IN_CATEGORY`), and applying strict integer cents comparisons (`priceInCents < value` for `IS_CHEAPER_THAN` and `priceInCents > value` for `IS_MORE_EXPENSIVE_THAN`).
- **FR-016**: The system MUST consider a campaign active at request time only when its `status` is `ACTIVE` and the current UTC timestamp falls within `[startDate, endDate]`.
- **FR-017**: The system MUST include a list of all matching active discounts (`campaignId`, `campaignName`, and `discountPercent`, or an empty list when none apply) in single-product retrieval and product-list external responses (across both v1 and v2 product reads) at request time, keeping `priceInCents` unmodified and storing no discount or campaign fields on the core `Product` domain entity.
- **FR-018**: The system MUST enforce perimeter contract validation and role-based authorization checks on all category, campaign, and product operations.

### Key Entities *(include if feature involves data)*

- **Category**: Represents a product classification group. Attributes: `id` (UUID, unique identifier), `name` (required non-blank text), `description` (optional text).
- **Product** (extended): Existing catalog product extended with `categoryId` (UUID referencing `Category`; optional in persistence and v1 operations, required in v2 operations). Core domain entity remains free of campaign or discount attributes.
- **Campaign**: Represents a time-bound promotional discount rule. Attributes: `id` (UUID, unique identifier), `name` (required non-blank text), `description` (optional text), `status` (required enum: `DRAFT`, `ACTIVE`, `INACTIVE`), `startDate` (required UTC DateTime), `endDate` (required UTC DateTime), `discountPercent` (required integer between 0 and 100), `predicates` (collection of `Predicate`).
- **Predicate**: Represents a matching criterion within a campaign. Attributes: `predicateType` (required enum: `IN_CATEGORY`, `NOT_IN_CATEGORY`, `IS_CHEAPER_THAN`, `IS_MORE_EXPENSIVE_THAN`), `value` (required text).
- **Product Discount View** (external response representation only): Request-time list of active discount entries (`campaignId`, `campaignName`, `discountPercent`) attached to product retrieval and listing outputs without computing a combined discounted price.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of category and campaign mutations, as well as campaign listings, reject callers who lack the `ADMIN` role, and 100% of category list requests reject callers who lack product-read access (`CUSTOMER` or `CATALOG`).
- **SC-002**: 100% of product creation and update attempts with an unknown `categoryId` are rejected before persistence, and 100% of deletion attempts on categories containing products are blocked with zero orphaned references.
- **SC-003**: 100% of campaign creations and updates enforce valid date windows, `discountPercent` bounds (0 to 100), and valid predicate definitions before persistence.
- **SC-004**: 100% of product single-item and list responses reflect currently active matching campaigns at request time, while non-active or out-of-window campaigns never appear.
- **SC-005**: The core `Product` domain entity stores no campaign or discount state, preserving complete separation between catalog persistence and request-time promotion evaluation.
- **SC-006**: 100% of existing v1 product creation, update, and read flows that omit `categoryId` continue to succeed without breaking changes, while 100% of v2 product creation and update requests lacking `categoryId` are rejected.

## Assumptions

- Category and campaign creation accept client-generated UUIDs, consistent with existing `Product`, `Order`, and `User` creation behavior in this repository.
- Campaign creation and update accept the full set of Campaign fields (`name`, `description`, `status`, `startDate`, `endDate`, `discountPercent`, and `predicates`, plus `id` on creation).
- All timestamps (`startDate`, `endDate`) and request-time active-window evaluations use UTC.
- No dedicated single-category retrieval operation or single-campaign retrieval operation is required; callers and product category validators use the category and campaign listing operations.
- Optional text filters (`name`, `description`) on category and campaign listings match case-insensitive substrings when provided, and blank or omitted filter parameters apply no filter.
- Campaign listing date filters (`startDate`, `endDate`) return campaigns whose date window overlaps or falls within the requested `[startDate, endDate]` bounds.
- Text length bounds follow existing `Product` conventions (`name` up to 255 characters, `description` up to 4000 characters), and category `name` values are not required to be globally unique unless restricted later.
- Price thresholds in `IS_CHEAPER_THAN` and `IS_MORE_EXPENSIVE_THAN` predicates express integer cents matching `Product.priceInCents`.
