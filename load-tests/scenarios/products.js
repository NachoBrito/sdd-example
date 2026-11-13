import http from 'k6/http';
import { check, sleep } from 'k6';
import { createProductPayload } from '../helpers/payloads.js';

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

export default function () {
  // 1. Browse active products catalog (50% weight)
  const listRes = http.get(
    `${TARGET_URL}/v1/products?limit=10&offset=0&status=ACTIVE`,
    { headers: { Accept: 'application/json' } }
  );

  check(listRes, {
    'list products status is 200': (r) => r.status === 200,
  });

  let knownProductId = null;
  if (listRes.status === 200) {
    try {
      const body = JSON.parse(listRes.body);
      const items = Array.isArray(body) ? body : (body.items || body.content || []);
      if (items.length > 0) {
        const item = items[Math.floor(Math.random() * items.length)];
        knownProductId = item.id;
      }
    } catch (e) {
      // Ignore JSON parse errors in body
    }
  }

  sleep(0.1);

  // 2. Retrieve product details by ID if an existing product is found (20% weight)
  if (knownProductId && Math.random() < 0.5) {
    const getRes = http.get(
      `${TARGET_URL}/v1/products/${knownProductId}`,
      { headers: { Accept: 'application/json' } }
    );

    check(getRes, {
      'get product status is 200': (r) => r.status === 200,
    });

    sleep(0.1);
  }

  // 3. Create a new product with realistic payload (30% weight)
  if (Math.random() < 0.4) {
    const payload = JSON.stringify(createProductPayload());
    const postRes = http.post(
      `${TARGET_URL}/v1/products`,
      payload,
      { headers: JSON_HEADERS }
    );

    check(postRes, {
      'create product status is 201': (r) => r.status === 201,
    });

    sleep(0.1);
  }
}

