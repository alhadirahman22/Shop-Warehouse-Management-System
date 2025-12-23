# Warehouse Inventory API

Simple warehouse inventory service built with Spring Boot, focused on SKU-level stock tracking, stock movements, and order flow.

## How to run the application

### Requirements

- Java 21
- Maven 3.9+
- Docker & Docker Compose
- MySQL 8.0+

### Production Mode

```bash
# Start MySQL
cd mysql
docker compose up -d

# Build and start the app (from code directory)
cd ..
docker compose -f docker-compose.prod.yml up -d --build
```

The app will be available at `http://localhost:8012`

### Development Mode

For local development with hot reload:

```bash
# Start MySQL first
cd mysql && docker compose up -d

# Start app in dev mode with debugger
cd ..
docker compose -f docker-compose.dev.yml up -d --build
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:mysql://database:13306/warehouse_inventory` | Database connection URL |
| `DB_USERNAME` | `sqluser` | Database user |
| `DB_PASSWORD` | `123sqly6` | Database password |
| `SERVER_PORT` | `8012` | Application port |

### Stopping Services

```bash
# Stop app
cd code && docker compose -f docker-compose.prod.yml down

# Stop MySQL
cd ../mysql && docker compose down
```


## Design Decisions
This section explains the main technical/architectural choices and why they were made (not just what was used).

### 1. Layered Architecture
Decision:
I used a layered architecture (Controller -> Service -> Repository).

Why:
To separate concerns, improve maintainability, and make the code easier to test.

### 2. DTOs for Request & Response
Decision:
I used DTOs for API requests and responses instead of exposing entities directly.

Why:
To avoid tight coupling with the database schema, enable clearer validation, and keep the API contract stable even when the database changes.

### 3. Consistent Response Envelope (`ApiResponse`)
Decision:
All endpoints return a consistent `ApiResponse` wrapper (`code`, `message`, `data`, `errors`).

Why:
So frontend/client code can handle success/errors in a uniform way across endpoints.

### 4. Flexible `getList` Querying (Pagination/Sort/Filters)
Decision:
The `getList` endpoints (items/variants/stock) support `offset`, `limit`, `sort_by`, `sort_direction`, optional `search`, and dynamic filters via `filters[n][field|operator|value]`.

Why:
To support many UI needs (tables, search, dropdowns) without creating many "custom list" endpoints.

### 5. Business Rules in the Service Layer
Decision:
Validations such as stock availability checks, duplicate checks (item/variant names), and guarded deletes are enforced in the service layer.

Why:
Business rules should live in services (not controllers) so they stay consistent, reusable, and easier to maintain.

### 6. Domain Modeling: Items vs Variants
Decision:
Items represent product families; variants represent SKU-level entries. Variant `attributes` are stored as JSON and sanitized against an allowlist.

Why:
To allow flexible SKU attributes while keeping stored data consistent and safe to query.

### 7. Stock Model + Audit Trail
Decision:
Stock is modeled as current quantity per variant plus an immutable `stock_movements` log. The `stock` table also enforces non-negative quantities via a database constraint (`CHECK (quantity >= 0)`).

Why:
To support auditability and reconciliation while still keeping "current stock" fast to read, with a database-level safety net that prevents negative stock.

### 8. Order Lifecycle + Stock Side Effects
Decision:
Orders move through `NEW` -> `PAID` -> `CANCELLED`. Stock is reduced on payment and replenished on cancel after payment.

Why:
To match real business flow and avoid reducing stock before an order is actually paid.

## Assumptions
- Java 21 is used to build and run the service (see `pom.xml`).
- The database is MySQL 8 (or compatible) and supports JSON columns and `json_extract` functions used in attribute filtering.
- Flyway migrations are enabled (`db/migration`); the database user has sufficient privileges to run them.
- The API is mounted under `/api` (`spring.mvc.servlet.path=/api`).
- The default Spring profile is `dev`; production expects `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
- Swagger/OpenAPI (springdoc) is enabled only in the `dev` profile (`application-dev.yml`) and disabled by default (`application.yml`). Swagger UI: `/api/swagger-ui/index.html`, OpenAPI JSON: `/api/v3/api-docs`.
- When running with the `dev` profile, a startup seeder (`DevDataSeeder`) inserts dummy items/variants/stock if the database is empty.
- Variant `attributes` are sent as a JSON string; invalid JSON or disallowed keys are ignored and stored as `null`.
- A stock row is auto-created when a variant is created; stock movements require the variant to already exist.
- Order creation validates available stock at that moment but does not reserve stock; stock can still change before payment.
- Examples use port `8012` from `application-dev.yml`; adjust the base URL for other profiles or environments.

## Project Resources
- ERD diagram: [`erd.pdf`](erd.pdf)
- Postman collection: [`Shop-Warehouse-Management-System.postman_collection.json`](Shop-Warehouse-Management-System.postman_collection.json) (import into Postman, then set the base URL to match your running environment)

## API Endpoint Examples

Base URL used below:
```bash
BASE_URL=http://localhost:8012/api
```

Create an item:
```bash
curl -X POST "$BASE_URL/items/create" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Cable",
    "description": "CAT6 cable",
    "active": true
  }'
```

List items with filters:
```bash
curl "$BASE_URL/items/getList?offset=0&limit=10&sort_by=id&sort_direction=asc&filters[0][field]=active&filters[0][operator]==&filters[0][value]=true"
```

Note for `getList` endpoints: URL-encode query params (especially `filters[...]` and JSON-like values) because characters like `[`, `]`, `{`, `}`, and `=` are reserved and may be altered by clients or proxies; encoding preserves the exact filter keys/values. Example: `filters[0][field]` -> `filters%5B0%5D%5Bfield%5D`.

Frontend dropdown usage (example: variant form item dropdown):
```bash
curl "$BASE_URL/items/getList?offset=0&limit=50&sort_by=name&sort_direction=asc&filters[0][field]=active&filters[0][operator]==&filters[0][value]=true"
```

Create a variant (attributes is a JSON string):
```bash
curl -X POST "$BASE_URL/variant/create" \
  -H "Content-Type: application/json" \
  -d '{
    "itemId": 1,
    "variantName": "Cable 10m",
    "attributes": "{\"length\":\"10m\",\"brand\":\"Belden\"}",
    "price": 55000,
    "active": true
  }'
```

Find a variant by SKU:
```bash
curl "$BASE_URL/variant/sku/SKU-123"
```

Stock in/out/adjust:
```bash
curl -X POST "$BASE_URL/inventory/in" \
  -H "Content-Type: application/json" \
  -d '{"variantId": 10, "quantity": 5, "referenceId": "PO-001"}'
```

```bash
curl -X POST "$BASE_URL/inventory/out" \
  -H "Content-Type: application/json" \
  -d '{"variantId": 10, "quantity": 2, "referenceId": "SO-045"}'
```

```bash
curl -X POST "$BASE_URL/inventory/adjust" \
  -H "Content-Type: application/json" \
  -d '{"variantId": 10, "changeQty": -1, "referenceId": "ADJ-202"}'
```

List stock:
```bash
curl "$BASE_URL/stock/getList?offset=0&limit=10&filters[0][field]=sku&filters[0][operator]==&filters[0][value]=SKU-123"
```

List stock movements:
```bash
curl "$BASE_URL/stock-movements/getList?offset=0&limit=10&search=SKU-123&sort_by=created_at&sort_direction=desc"
```

Create an order:
```bash
curl -X POST "$BASE_URL/orders/create" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      { "variantId": 10, "quantity": 2 },
      { "variantId": 12, "quantity": 1 }
    ]
  }'
```

Mark an order as paid / cancel an order:
```bash
curl -X PATCH "$BASE_URL/orders/pay/100"
```

```bash
curl -X PATCH "$BASE_URL/orders/cancel/100"
```

Response envelope example:
```json
{
  "code": "200",
  "message": "Success",
  "data": {
    "id": 1,
    "name": "Cable",
    "description": "CAT6 cable",
    "active": true
  },
  "errors": null,
  "timestamp": "2024-01-01T10:00:00Z"
}
```
