/**
 * Helper utilities and realistic synthetic payload generators for k6 load tests.
 */
import http from 'k6/http';

/**
 * Generates a compliant RFC 4122 version 4 UUID.
 *
 * @returns {string} Formatted UUIDv4 string
 */
export function uuidv4() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

/**
 * Generates a random alphanumeric string of the specified length.
 *
 * @param {number} length Desired string length
 * @returns {string} Random alphanumeric string
 */
export function randomString(length = 8) {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

/**
 * Generates a random integer between min and max (inclusive).
 *
 * @param {number} min Minimum integer
 * @param {number} max Maximum integer
 * @returns {number} Random integer
 */
export function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

const FIRST_NAMES = [
  'Alice', 'Bob', 'Carlos', 'David', 'Elena', 'Fatima', 'Gabriel',
  'Helena', 'Isabel', 'Javier', 'Klaus', 'Lucia', 'Mateo', 'Nadia'
];

const LAST_NAMES = [
  'Garcia', 'Rodriguez', 'Smith', 'Mueller', 'Dubois', 'Martin',
  'Silva', 'Hernandez', 'Lopez', 'Gonzalez', 'Perez', 'Sanchez'
];

const PRODUCT_ADJECTIVES = [
  'Ergonomic', 'Wireless', 'Mechanical', 'Compact', 'Precision',
  'Ultra-light', 'Durable', 'Professional', 'Silent', 'Modular'
];

const PRODUCT_NOUNS = [
  'Keyboard', 'Mouse', 'Monitor', 'Headset', 'Webcam',
  'Docking Station', 'Microphone', 'Desk Mat', 'USB Hub', 'Trackball'
];

const CITIES = [
  { city: 'Madrid', state: 'Madrid', zipCode: '28013', country: 'Spain' },
  { city: 'Barcelona', state: 'Catalunya', zipCode: '08001', country: 'Spain' },
  { city: 'Valencia', state: 'Valencia', zipCode: '46001', country: 'Spain' },
  { city: 'Sevilla', state: 'Andalucia', zipCode: '41001', country: 'Spain' },
  { city: 'Bilbao', state: 'Bizkaia', zipCode: '48001', country: 'Spain' }
];

/**
 * Creates a realistic product creation payload.
 *
 * @param {Object} [overrides] Optional attribute overrides
 * @returns {Object} Product payload matching the schema
 */
export function createProductPayload(overrides = {}) {
  const id = overrides.id || uuidv4();
  const adj = PRODUCT_ADJECTIVES[Math.floor(Math.random() * PRODUCT_ADJECTIVES.length)];
  const noun = PRODUCT_NOUNS[Math.floor(Math.random() * PRODUCT_NOUNS.length)];
  const code = randomString(6).toUpperCase();

  return {
    id: id,
    sku: `PROD-${code}`,
    name: `${adj} ${noun} ${code}`,
    description: `High performance ${adj.toLowerCase()} ${noun.toLowerCase()} with premium components and ergonomic design.`,
    priceInCents: randomInt(1999, 49999),
    currency: 'EUR',
    status: 'ACTIVE',
    ...overrides
  };
}

/**
 * Creates a realistic order creation payload.
 *
 * @param {Array<string>} productIds Valid product UUIDs to include in order lines
 * @param {Object} [overrides] Optional attribute overrides
 * @returns {Object} Order payload matching the schema
 */
export function createOrderPayload(productIds, overrides = {}) {
  if (!productIds || productIds.length === 0) {
    throw new Error('Order creation requires at least one valid product identifier.');
  }

  const id = overrides.id || uuidv4();
  const firstName = FIRST_NAMES[Math.floor(Math.random() * FIRST_NAMES.length)];
  const lastName = LAST_NAMES[Math.floor(Math.random() * LAST_NAMES.length)];
  const customerName = `${firstName} ${lastName}`;
  const customerEmail = `${firstName.toLowerCase()}.${lastName.toLowerCase()}.${randomString(4)}@example.com`;
  const location = CITIES[Math.floor(Math.random() * CITIES.length)];
  const streetNum = randomInt(1, 250);

  // Pick 1 to 3 products from available product IDs
  const numItems = Math.min(productIds.length, randomInt(1, 3));
  const selectedProductIds = [...productIds]
    .sort(() => 0.5 - Math.random())
    .slice(0, numItems);

  const orderLines = selectedProductIds.map((prodId) => ({
    productId: prodId,
    quantity: randomInt(1, 5),
    unitPriceInCents: randomInt(1999, 19999)
  }));

  return {
    id: id,
    customerName: customerName,
    customerEmail: customerEmail,
    currency: 'EUR',
    deliveryAddress: {
      name: customerName,
      telephone: `+34-91-${randomInt(100, 999)}-${randomInt(1000, 9999)}`,
      street: `${streetNum} Calle Principal`,
      complement: `Piso ${randomInt(1, 9)}`,
      city: location.city,
      state: location.state,
      zipCode: location.zipCode,
      country: location.country
    },
    invoiceAddress: {
      name: customerName,
      vatNumber: `ES${randomInt(10000000, 99999999)}Z`,
      telephone: `+34-91-${randomInt(100, 999)}-${randomInt(1000, 9999)}`,
      street: `${streetNum} Calle Principal`,
      complement: `Piso ${randomInt(1, 9)}`,
      city: location.city,
      state: location.state,
      zipCode: location.zipCode,
      country: location.country
    },
    orderLines: orderLines,
    ...overrides
  };
}

/**
 * Creates a user registration payload with a unique UUID and username.
 *
 * @param {Object} [overrides] Optional attribute overrides
 * @returns {Object} Registration payload
 */
export function createRegisterPayload(overrides = {}) {
  const suffix = randomString(10).toLowerCase();
  return {
    userId: overrides.userId || uuidv4(),
    username: overrides.username || `loaduser_${suffix}`,
    password: overrides.password || 'LoadTestPass123!',
    ...overrides
  };
}

/**
 * Authenticates against /v1/auth/login and returns the access and refresh tokens.
 *
 * @param {string} targetUrl Base service URL
 * @param {string} username Account username
 * @param {string} password Account password
 * @returns {{ accessToken: string, refreshToken: string }} Issued JWT tokens
 */
export function login(targetUrl, username, password) {
  const res = http.post(
    `${targetUrl}/v1/auth/login`,
    JSON.stringify({ username: username, password: password }),
    { headers: { 'Content-Type': 'application/json', Accept: 'application/json' } }
  );
  if (res.status !== 200) {
    throw new Error(`Login failed for ${username} with status ${res.status}: ${res.body}`);
  }
  const body = JSON.parse(res.body);
  return { accessToken: body.accessToken, refreshToken: body.refreshToken };
}

/**
 * Builds JSON request headers with a Bearer authorization token.
 *
 * @param {string} token JWT access token
 * @returns {Object} HTTP headers map
 */
export function authHeaders(token) {
  return {
    'Content-Type': 'application/json',
    Accept: 'application/json',
    Authorization: `Bearer ${token}`
  };
}

