package es.nachobrito.sdd.example.adapter.out.smtp;

import es.nachobrito.sdd.example.domain.model.DeliveryAddress;
import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderLifecycleEvent;
import es.nachobrito.sdd.example.domain.model.OrderLine;
import jakarta.inject.Singleton;
import java.util.Locale;
import java.util.Objects;

/**
 * Renders HTML email templates for order lifecycle events.
 */
@Singleton
public class SmtpOrderHtmlTemplate {

  /**
   * Renders the subject line for the email notification.
   *
   * @param event the order lifecycle event
   * @return the rendered subject string
   */
  public String renderSubject(OrderLifecycleEvent event) {
    Objects.requireNonNull(event, "Event must not be null");
    String prefix = switch (event.eventType()) {
      case CREATED -> "Order Confirmation: ";
      case UPDATED -> "Order Update: ";
      case CANCELLED -> "Order Cancellation: ";
    };
    return prefix + event.order().id();
  }

  /**
   * Renders the HTML body content for the email notification.
   *
   * @param event the order lifecycle event
   * @return the rendered HTML string
   */
  public String renderBody(OrderLifecycleEvent event) {
    Objects.requireNonNull(event, "Event must not be null");
    Order order = event.order();

    String title = switch (event.eventType()) {
      case CREATED -> "Order Confirmation";
      case UPDATED -> "Order Update Notification";
      case CANCELLED -> "Order Cancellation Notification";
    };

    StringBuilder html = new StringBuilder(2048);
    html.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n<title>")
        .append(HtmlEscaper.escape(title))
        .append("</title>\n<style>\n"
            + "body { font-family: Arial, sans-serif; color: #333; line-height: 1.5; }\n"
            + ".card { border: 1px solid #ddd; padding: 20px; border-radius: 6px; }\n"
            + "table { width: 100%; border-collapse: collapse; margin-top: 15px; }\n"
            + "th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n"
            + "th { background-color: #f7f7f7; }\n"
            + "</style>\n</head>\n<body>\n<div class=\"card\">\n<h2>")
        .append(HtmlEscaper.escape(title))
        .append("</h2>\n<p>Dear <strong>")
        .append(HtmlEscaper.escape(order.customerName()))
        .append("</strong>,</p>\n");

    renderMessageIntro(html, event);
    renderOrderSummary(html, order);
    renderOrderLines(html, order);
    renderDeliveryAddress(html, order.deliveryAddress());

    html.append("<p>Thank you for choosing our store.</p>\n</div>\n</body>\n</html>");

    return html.toString();
  }

  private void renderMessageIntro(StringBuilder html, OrderLifecycleEvent event) {
    String intro = switch (event.eventType()) {
      case CREATED -> "<p>Your order has been received successfully.</p>\n";
      case UPDATED -> "<p>Your order details have been updated.</p>\n";
      case CANCELLED -> "<p>Your order has been cancelled.</p>\n";
    };
    html.append(intro);
  }

  private void renderOrderSummary(StringBuilder html, Order order) {
    String totalFormatted = String.format(
        Locale.US, "%.2f %s", order.totalAmountInCents() / 100.0, order.currency());

    html.append("<p><strong>Order ID:</strong> ").append(order.id())
        .append("<br/>\n<strong>Status:</strong> ")
        .append(HtmlEscaper.escape(order.status().name()))
        .append("<br/>\n<strong>Date:</strong> ").append(order.date())
        .append("<br/>\n<strong>Total Amount:</strong> ").append(totalFormatted).append("</p>\n");
  }

  private void renderOrderLines(StringBuilder html, Order order) {
    html.append("<h3>Order Items</h3>\n<table>\n<thead>\n<tr>\n"
        + "<th>Product ID</th><th>Quantity</th><th>Unit Price</th><th>Line Total</th>\n"
        + "</tr>\n</thead>\n<tbody>\n");

    for (OrderLine line : order.orderLines()) {
      String unitPrice = String.format(
          Locale.US, "%.2f %s", line.unitPriceInCents() / 100.0, order.currency());
      String lineTotal = String.format(
          Locale.US, "%.2f %s", line.lineTotalInCents() / 100.0, order.currency());

      html.append("<tr>\n<td>").append(line.productId())
          .append("</td>\n<td>").append(line.quantity())
          .append("</td>\n<td>").append(unitPrice)
          .append("</td>\n<td>").append(lineTotal)
          .append("</td>\n</tr>\n");
    }

    html.append("</tbody>\n</table>\n");
  }

  private void renderDeliveryAddress(StringBuilder html, DeliveryAddress address) {
    html.append("<h3>Delivery Address</h3>\n<p>")
        .append(HtmlEscaper.escape(address.name()))
        .append("<br/>\n")
        .append(HtmlEscaper.escape(address.street()))
        .append("<br/>\n");

    String complement = address.complement();
    if (complement != null && !complement.isBlank()) {
      html.append(HtmlEscaper.escape(complement)).append("<br/>\n");
    }

    html.append(HtmlEscaper.escape(address.city())).append(", ")
        .append(HtmlEscaper.escape(address.state())).append(' ')
        .append(HtmlEscaper.escape(address.zipCode()))
        .append("<br/>\n")
        .append(HtmlEscaper.escape(address.country()))
        .append("<br/>\nTelephone: ")
        .append(HtmlEscaper.escape(address.telephone())).append("</p>\n");
  }
}
