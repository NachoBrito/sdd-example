# Data Model: Product Categories and Discount Campaigns

**Feature**: `012-categories-campaigns` | **Date**: 2026-11-14

## Domain Entities & Value Objects (`es.nachobrito.sdd.example.domain.model`)

All domain types remain pure Java with zero framework imports (only `java..` and `org.jspecify..`).

### 1. `Category`
- **Type**: Java `record`
- **Fields**:
  - `UUID id` (required, non-null, client-generated)
  - `String name` (required, non-blank, trimmed, max 255 characters)
  - `@Nullable String description` (optional, trimmed when non-null, max 4000 characters)
- **Validation Rules**:
  - `id == null` -> `InvalidCategoryDataException`
  - `name == null || name.isBlank()` or trimmed length > 255 -> `InvalidCategoryDataException`
  - `description != null` and trimmed length > 4000 -> `InvalidCategoryDataException`

### 2. `Product` (extended)
- **Type**: Java `record` (existing aggregate root)
- **Added Field**:
  - `@Nullable UUID categoryId` (optional in domain & v1 operations; required at `/v2/products` contract boundary)
- **Invariant Changes**:
  - `Product` stores no campaign or discount attributes.
  - Existing compact-constructor invariants remain unchanged.
  - `Product.create(...)` and `Product.withUpdatedAttributes(...)` accept `@Nullable UUID categoryId` (preserving convenience overloads or updating callers cleanly).

### 3. `CampaignStatus`
- **Type**: Java `enum`
- **Values**: `DRAFT`, `ACTIVE`, `INACTIVE`

### 4. `PredicateType`
- **Type**: Java `enum`
- **Values**: `IN_CATEGORY`, `NOT_IN_CATEGORY`, `IS_CHEAPER_THAN`, `IS_MORE_EXPENSIVE_THAN`

### 5. `CampaignPredicate`
- **Type**: Java `record`
- **Fields**:
  - `PredicateType predicateType` (required, non-null)
  - `String value` (required, non-blank, trimmed, max 255 characters)
- **Validation Rules**:
  - `predicateType == null` -> `InvalidCampaignDataException`
  - `value == null || value.isBlank()` or trimmed length > 255 -> `InvalidCampaignDataException`
  - When `predicateType` is `IN_CATEGORY` or `NOT_IN_CATEGORY`: `value` must parse via `UUID.fromString(value)`; otherwise -> `InvalidCampaignDataException`.
  - When `predicateType` is `IS_CHEAPER_THAN` or `IS_MORE_EXPENSIVE_THAN`: `value` must parse as a non-negative integer (`Long.parseLong(value) >= 0`); otherwise -> `InvalidCampaignDataException`.
- **Evaluation Rules (`matches(Product product)`)**:
  - `IN_CATEGORY`: returns `product.categoryId() != null && product.categoryId().equals(UUID.fromString(value))`
  - `NOT_IN_CATEGORY`: returns `product.categoryId() == null || !product.categoryId().equals(UUID.fromString(value))`
  - `IS_CHEAPER_THAN`: returns `product.priceInCents() < Long.parseLong(value)`
  - `IS_MORE_EXPENSIVE_THAN`: returns `product.priceInCents() > Long.parseLong(value)`

### 6. `Campaign`
- **Type**: Java `record`
- **Fields**:
  - `UUID id` (required, non-null, client-generated)
  - `String name` (required, non-blank, trimmed, max 255 characters)
  - `@Nullable String description` (optional, trimmed when non-null, max 4000 characters)
  - `CampaignStatus status` (required, non-null)
  - `Instant startDate` (required, non-null, UTC)
  - `Instant endDate` (required, non-null, UTC)
  - `int discountPercent` (required, integer in `[0, 100]`)
  - `List<CampaignPredicate> predicates` (non-null immutable list; empty list permitted)
- **Validation Rules**:
  - `id == null` -> `InvalidCampaignDataException`
  - `name == null || name.isBlank()` or trimmed length > 255 -> `InvalidCampaignDataException`
  - `description != null` and trimmed length > 4000 -> `InvalidCampaignDataException`
  - `status == null` -> `InvalidCampaignDataException`
  - `startDate == null` or `endDate == null` -> `InvalidCampaignDataException`
  - `endDate.isBefore(startDate)` -> `InvalidCampaignDataException`
  - `discountPercent < 0 || discountPercent > 100` -> `InvalidCampaignDataException`
  - `predicates`: defensively copied (`predicates == null ? List.of() : List.copyOf(predicates)`); no null elements allowed.
- **State & Matching Rules**:
  - `isActiveAt(Instant now)`: `status == CampaignStatus.ACTIVE && !now.isBefore(startDate) && !now.isAfter(endDate)`
  - `matches(Product product)`: returns `true` if `predicates.isEmpty()` or every predicate in `predicates` returns `true` for `product` (logical `AND`).

### 7. `ActiveDiscount`
- **Type**: Java `record` (lightweight read-projection value object used for request-time discount output; never stored on `Product`)
- **Fields**:
  - `UUID campaignId`
  - `String campaignName`
  - `int discountPercent`

### 8. Domain Exceptions
- `CategoryException` (base) -> `InvalidCategoryDataException` (`400`), `CategoryNotFoundException` (`404`), `DuplicateCategoryIdentifierException` (`409`), `CategoryInUseException` (`409`).
- `CampaignException` (base) -> `InvalidCampaignDataException` (`400`), `CampaignNotFoundException` (`404`), `DuplicateCampaignIdentifierException` (`409`).

---

## Database Schema Changes (Liquibase: `005-add-categories-and-campaigns.xml`)

### 1. `categories` Table
| Column | Type | Constraints |
|---|---|---|
| `id` | `VARCHAR(36)` | `PRIMARY KEY`, `NOT NULL` |
| `name` | `VARCHAR(255)` | `NOT NULL` |
| `description` | `TEXT` | `NULL` |

- Index: `idx_categories_name` on `(name)`

### 2. `products` Table Alteration (Expand Step)
| Column | Type | Constraints |
|---|---|---|
| `category_id` | `VARCHAR(36)` | `NULL`, `FOREIGN KEY fk_products_category REFERENCES categories(id) ON DELETE RESTRICT` |

- Index: `idx_products_category_id` on `(category_id)`

### 3. `campaigns` Table
| Column | Type | Constraints |
|---|---|---|
| `id` | `VARCHAR(36)` | `PRIMARY KEY`, `NOT NULL` |
| `name` | `VARCHAR(255)` | `NOT NULL` |
| `description` | `TEXT` | `NULL` |
| `status` | `VARCHAR(20)` | `NOT NULL` |
| `start_date` | `TIMESTAMP(6)` | `NOT NULL` |
| `end_date` | `TIMESTAMP(6)` | `NOT NULL` |
| `discount_percent` | `INT` | `NOT NULL` |

- Check constraints:
  - `chk_campaigns_discount_percent`: `CHECK (discount_percent >= 0 AND discount_percent <= 100)`
  - `chk_campaigns_date_range`: `CHECK (end_date >= start_date)`
- Index: `idx_campaigns_status_dates` on `(status, start_date, end_date)`

### 4. `campaign_predicates` Table
| Column | Type | Constraints |
|---|---|---|
| `id` | `VARCHAR(36)` | `PRIMARY KEY`, `NOT NULL` |
| `campaign_id` | `VARCHAR(36)` | `NOT NULL`, `FOREIGN KEY fk_campaign_predicates_campaign REFERENCES campaigns(id) ON DELETE CASCADE` |
| `predicate_type` | `VARCHAR(32)` | `NOT NULL` |
| `predicate_value` | `VARCHAR(255)` | `NOT NULL` |

- Index: `idx_campaign_predicates_campaign_id` on `(campaign_id)`

