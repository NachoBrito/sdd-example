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
const DURATION = __ENV.DURATION || '5m';
const VUS = parseInt(__ENV.VUS || '10', 10);

export const options = {
  vus: VUS,
  duration: DURATION,
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
  },
};

/**
 * Setup lifecycle: authenticates and populates initial products for sustained operations.
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
  const actionRoll = Math.random();

  if (actionRoll < 0.50) {
    // 1. Catalog Browsing
    const res = http.get(
      `${TARGET_URL}/v1/products?limit=10&offset=0&status=ACTIVE`,
      { headers: headers }
    );
    check(res, { 'status is 200': (r) => r.status === 200 });
  } else if (actionRoll < 0.75) {
    // 2. Product Detail
    if (productIds.length > 0) {
      const pid = productIds[Math.floor(Math.random() * productIds.length)];
      const res = http.get(
        `${TARGET_URL}/v1/products/${pid}`,
        { headers: headers }
      );
      check(res, { 'status is 200': (r) => r.status === 200 });
    }
  } else if (actionRoll < 0.90) {
    // 3. Order Checkout
    if (productIds.length > 0) {
      const orderPayload = JSON.stringify(createOrderPayload(productIds));
      const res = http.post(
        `${TARGET_URL}/v1/orders`,
        orderPayload,
        { headers: headers }
      );
      check(res, { 'order created is 201': (r) => r.status === 201 });
    }
  } else {
    // 4. Product Creation
    const payload = JSON.stringify(createProductPayload());
    const res = http.post(
      `${TARGET_URL}/v1/products`,
      payload,
      { headers: headers }
    );
    const created = check(res, { 'product created is 201': (r) => r.status === 201 });
    if (created && res.status === 201) {
      try {
        const p = JSON.parse(res.body);
        if (p.id && productIds.length < 150) {
          productIds.push(p.id);
        }
      } catch (e) {
        // Ignore
      }
    }
  }

  // Periodic Health Check verification (1 out of ~20 iterations)
  if (Math.random() < 0.05) {
    const healthRes = http.get(`${TARGET_URL}/health`);
    check(healthRes, { 'health check is 200': (r) => r.status === 200 });
  }

  sleep(0.1 + Math.random() * 0.1);
}

/**
 * Teardown lifecycle: verifies health after sustained load completion.
 */
export function teardown() {
  const healthRes = http.get(`${TARGET_URL}/health`);
  check(healthRes, {
    'post-test health check is UP': (r) => r.status === 200 && r.json('status') === 'UP',
  });
}

