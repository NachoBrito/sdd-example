package es.nachobrito.sdd.example.domain.model;

/**
 * Represents the lifecycle status of an order.
 */
public enum OrderStatus {
  /** The order is created and waits for payment. */
  PENDING,

  /** The order is paid and ready for shipment. */
  PAID,

  /** The carrier transports the order to the customer. */
  SHIPPED,

  /** The carrier delivered the order to the customer. */
  DELIVERED,

  /** The order is cancelled. */
  CANCELLED
}

