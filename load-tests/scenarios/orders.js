import http from 'k6/http';
import { check, sleep } from 'k6';
import { createProductPayload, createOrderPayload } from '../helpers/payloads.js';

const TARGET_URL = __ENV.TARGET_URL || 'http://localhost:8080';

export const options = {
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
  },
};

const JSON_HEADERS = {
  'Content-Type': 'application/json',
  Accept: 'application/json',
};

/**
 * Setup lifecycle: ensures valid product identifiers exist in the database
 * before virtual users begin submitting orders.
 */
export function setup() {
  const productIds = [];

  // Query existing active products
  const listRes = http.get(
    `${TARGET_URL}/v1/products?limit=20&offset=0&status=ACTIVE`,
    { headers: { Accept: 'application/json' } }
  );

  if (listRes.status === 200) {
    try {
      const body = JSON.parse(listRes.body);
      const items = Array.isArray(body) ? body : (body.items || body.content || []);
      for (const item of items) {
        if (item.id) {
          productIds.push(item.id);
        }
      }
    } catch (e) {
      // Ignore parse errors
    }
  }

  // If fewer than 3 products exist, seed new products
  while (productIds.length < 3) {
    const payload = JSON.stringify(createProductPayload());
    const postRes = http.post(
      `${TARGET_URL}/v1/products`,
      payload,
      { headers: JSON_HEADERS }
    );

    if (postRes.status === 201) {
      try {
        const created = JSON.parse(postRes.body);
        productIds.push(created.id);
      } catch (e) {
        // Fallback
      }
    }
  }

  return { productIds: productIds };
}

export default function (data) {
  const productIds = data.productIds || [];
  if (productIds.length === 0) {
    throw new Error('No valid product identifiers available for order processing scenario.');
  }

  // 1. Create Order with realistic payload referencing valid product IDs (50% weight)
  const orderPayload = JSON.stringify(createOrderPayload(productIds));
  const postRes = http.post(
    `${TARGET_URL}/v1/orders`,
    orderPayload,
    { headers: JSON_HEADERS }
  );

  const orderCreated = check(postRes, {
    'create order status is 201': (r) => r.status === 201,
  });

  let createdOrderId = null;
  if (orderCreated && postRes.status === 201) {
    try {
      const orderBody = JSON.parse(postRes.body);
      createdOrderId = orderBody.id;
    } catch (e) {
      // Ignore parse error
    }
  }

  sleep(0.1);

  // 2. Retrieve the created order by identifier (30% weight)
  if (createdOrderId) {
    const getRes = http.get(
      `${TARGET_URL}/v1/orders/${createdOrderId}`,
      { headers: { Accept: 'application/json' } }
    );

    check(getRes, {
      'get order status is 200': (r) => r.status === 200,
    });

    sleep(0.1);
  }

  // 3. List recent orders (20% weight)
  if (Math.random() < 0.4) {
    const listRes = http.get(
      `${TARGET_URL}/v1/orders?limit=10&offset=0`,
      { headers: { Accept: 'application/json' } }
    );

    check(listRes, {
      'list orders status is 200': (r) => r.status === 200,
    });

    sleep(0.1);
  }
}

