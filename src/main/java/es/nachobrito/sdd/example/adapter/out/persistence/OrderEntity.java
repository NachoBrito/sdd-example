package es.nachobrito.sdd.example.adapter.out.persistence;

import es.nachobrito.sdd.example.domain.model.DeliveryAddress;
import es.nachobrito.sdd.example.domain.model.InvoiceAddress;
import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderLine;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Persistence entity mapping the orders table.
 *
 * @param id the order primary key
 * @param customerName the customer name
 * @param customerEmail the customer email
 * @param orderDate the order timestamp
 * @param status the order status string
 * @param totalAmountInCents the total amount in cents
 * @param currency the ISO currency code
 * @param deliveryName the delivery contact name
 * @param deliveryTelephone the delivery telephone
 * @param deliveryStreet the delivery street
 * @param deliveryComplement the delivery complement
 * @param deliveryCity the delivery city
 * @param deliveryState the delivery state
 * @param deliveryZipCode the delivery postal code
 * @param deliveryCountry the delivery country
 * @param invoiceName the invoice recipient name
 * @param invoiceVatNumber the invoice VAT identifier
 * @param invoiceTelephone the invoice telephone
 * @param invoiceStreet the invoice street
 * @param invoiceComplement the invoice complement
 * @param invoiceCity the invoice city
 * @param invoiceState the invoice state
 * @param invoiceZipCode the invoice postal code
 * @param invoiceCountry the invoice country
 * @param createdAt the creation timestamp
 * @param updatedAt the update timestamp
 */
@MappedEntity("orders")
public record OrderEntity(
    @Id @NonNull UUID id,
    @NonNull @MappedProperty("customer_name") String customerName,
    @NonNull @MappedProperty("customer_email") String customerEmail,
    @NonNull @MappedProperty("order_date") Instant orderDate,
    @NonNull String status,
    @MappedProperty("total_amount_in_cents") long totalAmountInCents,
    @NonNull String currency,
    @NonNull @MappedProperty("delivery_name") String deliveryName,
    @NonNull @MappedProperty("delivery_telephone") String deliveryTelephone,
    @NonNull @MappedProperty("delivery_street") String deliveryStreet,
    @Nullable @MappedProperty("delivery_complement") String deliveryComplement,
    @NonNull @MappedProperty("delivery_city") String deliveryCity,
    @NonNull @MappedProperty("delivery_state") String deliveryState,
    @NonNull @MappedProperty("delivery_zip_code") String deliveryZipCode,
    @NonNull @MappedProperty("delivery_country") String deliveryCountry,
    @NonNull @MappedProperty("invoice_name") String invoiceName,
    @NonNull @MappedProperty("invoice_vat_number") String invoiceVatNumber,
    @NonNull @MappedProperty("invoice_telephone") String invoiceTelephone,
    @NonNull @MappedProperty("invoice_street") String invoiceStreet,
    @Nullable @MappedProperty("invoice_complement") String invoiceComplement,
    @NonNull @MappedProperty("invoice_city") String invoiceCity,
    @NonNull @MappedProperty("invoice_state") String invoiceState,
    @NonNull @MappedProperty("invoice_zip_code") String invoiceZipCode,
    @NonNull @MappedProperty("invoice_country") String invoiceCountry,
    @NonNull @MappedProperty("created_at") Instant createdAt,
    @NonNull @MappedProperty("updated_at") Instant updatedAt) {

  /**
   * Converts this entity and its child lines to a domain Order aggregate.
   *
   * @param lines the order lines
   * @return the domain Order aggregate
   */
  public Order toDomain(List<OrderLine> lines) {
    DeliveryAddress delivery = new DeliveryAddress(
        this.deliveryName,
        this.deliveryTelephone,
        this.deliveryStreet,
        this.deliveryComplement,
        this.deliveryCity,
        this.deliveryState,
        this.deliveryZipCode,
        this.deliveryCountry);

    InvoiceAddress invoice = new InvoiceAddress(
        this.invoiceName,
        this.invoiceVatNumber,
        this.invoiceTelephone,
        this.invoiceStreet,
        this.invoiceComplement,
        this.invoiceCity,
        this.invoiceState,
        this.invoiceZipCode,
        this.invoiceCountry);

    return new Order(
        this.id,
        this.customerName,
        this.customerEmail,
        this.orderDate,
        OrderStatus.valueOf(this.status),
        this.totalAmountInCents,
        this.currency,
        delivery,
        invoice,
        lines,
        this.createdAt,
        this.updatedAt);
  }

  /**
   * Creates a persistence entity from a domain Order aggregate.
   *
   * @param domain the domain Order
   * @return the persistence entity
   */
  public static OrderEntity fromDomain(Order domain) {
    DeliveryAddress delivery = domain.deliveryAddress();
    InvoiceAddress invoice = domain.invoiceAddress();

    return new OrderEntity(
        domain.id(),
        domain.customerName(),
        domain.customerEmail(),
        domain.date(),
        domain.status().name(),
        domain.totalAmountInCents(),
        domain.currency(),
        delivery.name(),
        delivery.telephone(),
        delivery.street(),
        delivery.complement(),
        delivery.city(),
        delivery.state(),
        delivery.zipCode(),
        delivery.country(),
        invoice.name(),
        invoice.vatNumber(),
        invoice.telephone(),
        invoice.street(),
        invoice.complement(),
        invoice.city(),
        invoice.state(),
        invoice.zipCode(),
        invoice.country(),
        domain.createdAt(),
        domain.updatedAt());
  }
}

