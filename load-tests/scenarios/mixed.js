import http from 'k6/http';
import { check, sleep } from 'k6';
import {
  authHeaders,
  createProductPayload,
  createOrderPayload,
  login
} from '../helpers/payloads.js';

const TARGET_URL = __ENV.TARGET_URL || 'http://localhost:8080';
const ADMIN_USER = __ENV.ADMIN_USER || 'admin';
const ADMIN_PASS = __ENV.ADMIN_PASS || 'admin1234';

export const options = {
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
  },
};

/**
 * Setup lifecycle: authenticates and acquires a pool of active product identifiers
 * before user simulation begins.
 */
export function setup() {
  const tokens = login(TARGET_URL, ADMIN_USER, ADMIN_PASS);
  const headers = authHeaders(tokens.accessToken);
  const productIds = [];

  const listRes = http.get(
    `${TARGET_URL}/v1/products?limit=20&offset=0&status=ACTIVE`,
    { headers: headers }
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
      // Ignore
    }
  }

  while (productIds.length < 5) {
    const payload = JSON.stringify(createProductPayload());
    const postRes = http.post(
      `${TARGET_URL}/v1/products`,
      payload,
      { headers: headers }
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

  return { productIds: productIds, token: tokens.accessToken };
}

export default function (data) {
  const headers = authHeaders(data.token);
  const productIds = data.productIds || [];
  const roll = Math.random();

  if (roll < 0.60) {
    // 1. Catalog Browsing (60% of requests)
    const listRes = http.get(
      `${TARGET_URL}/v1/products?limit=10&offset=0&status=ACTIVE`,
      { headers: headers }
    );

    check(listRes, {
      'browse catalog status is 200': (r) => r.status === 200,
    });
  } else if (roll < 0.80) {
    // 2. Product Detail Lookup (20% of requests)
    if (productIds.length > 0) {
      const randomProductId = productIds[Math.floor(Math.random() * productIds.length)];
      const getRes = http.get(
        `${TARGET_URL}/v1/products/${randomProductId}`,
        { headers: headers }
      );

      check(getRes, {
        'view product detail status is 200': (r) => r.status === 200,
      });
    }
  } else if (roll < 0.90) {
    // 3. Product Catalog Addition (10% of requests)
    const payload = JSON.stringify(createProductPayload());
    const postRes = http.post(
      `${TARGET_URL}/v1/products`,
      payload,
      { headers: headers }
    );

    const created = check(postRes, {
      'add product status is 201': (r) => r.status === 201,
    });

    if (created && postRes.status === 201) {
      try {
        const newProduct = JSON.parse(postRes.body);
        if (newProduct.id && productIds.length < 100) {
          productIds.push(newProduct.id);
        }
      } catch (e) {
        // Ignore
      }
    }
  } else {
    // 4. Order Checkout (10% of requests)
    if (productIds.length > 0) {
      const orderPayload = JSON.stringify(createOrderPayload(productIds));
      const postRes = http.post(
        `${TARGET_URL}/v1/orders`,
        orderPayload,
        { headers: headers }
      );

      const orderCreated = check(postRes, {
        'checkout order status is 201': (r) => r.status === 201,
      });

      if (orderCreated && postRes.status === 201) {
        try {
          const orderBody = JSON.parse(postRes.body);
          if (orderBody.id) {
            const getOrderRes = http.get(
              `${TARGET_URL}/v1/orders/${orderBody.id}`,
              { headers: headers }
            );

            check(getOrderRes, {
              'verify created order status is 200': (r) => r.status === 200,
            });
          }
        } catch (e) {
          // Ignore
        }
      }
    }
  }

  // Realistic human think time
  sleep(0.1 + Math.random() * 0.1);
}

