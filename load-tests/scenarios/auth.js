import http from 'k6/http';
import { check, sleep } from 'k6';
import { authHeaders, createRegisterPayload } from '../helpers/payloads.js';

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
  const userPayload = createRegisterPayload();

  // 1. Register a new CUSTOMER user
  const regRes = http.post(
    `${TARGET_URL}/v1/auth/register`,
    JSON.stringify(userPayload),
    { headers: JSON_HEADERS }
  );
  check(regRes, { 'register status is 201': (r) => r.status === 201 });

  sleep(0.05);

  // 2. Login to obtain access and refresh tokens
  const loginRes = http.post(
    `${TARGET_URL}/v1/auth/login`,
    JSON.stringify({ username: userPayload.username, password: userPayload.password }),
    { headers: JSON_HEADERS }
  );
  const loggedIn = check(loginRes, { 'login status is 200': (r) => r.status === 200 });
  if (!loggedIn) {
    return;
  }

  const tokens = JSON.parse(loginRes.body);
  sleep(0.05);

  // 3. Validate access token
  const validateRes = http.post(
    `${TARGET_URL}/v1/auth/validate`,
    JSON.stringify({}),
    { headers: authHeaders(tokens.accessToken) }
  );
  check(validateRes, { 'validate status is 200': (r) => r.status === 200 });

  sleep(0.05);

  // 4. Refresh token pair
  const refreshRes = http.post(
    `${TARGET_URL}/v1/auth/refresh`,
    JSON.stringify({ refreshToken: tokens.refreshToken }),
    { headers: JSON_HEADERS }
  );
  const refreshed = check(refreshRes, { 'refresh status is 200': (r) => r.status === 200 });
  const activeAccess = refreshed ? JSON.parse(refreshRes.body).accessToken : tokens.accessToken;

  sleep(0.05);

  // 5. Revoke access token
  const revokeRes = http.post(
    `${TARGET_URL}/v1/auth/revoke`,
    JSON.stringify({}),
    { headers: authHeaders(activeAccess) }
  );
  check(revokeRes, { 'revoke status is 204': (r) => r.status === 204 });

  sleep(0.1);
}

