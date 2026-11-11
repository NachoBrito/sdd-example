package es.nachobrito.sdd.example.integration;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * End-to-end integration test verifying asynchronous order event notifications.
 */
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationFlowTestCase implements TestPropertyProvider {

  private static final String CMD_DATA = "DATA";
  private static final String CMD_QUIT = "QUIT";
  private static final String CHAR_DOT = ".";

  private static final ReentrantLock SERVER_LOCK = new ReentrantLock();
  private static final AtomicBoolean RUNNING = new AtomicBoolean(true);
  private static final BlockingQueue<String> RECEIVED_EMAILS = new LinkedBlockingQueue<>();
  private static ServerSocket smtpServerSocket;
  private static int smtpPort;

  @Override
  public Map<String, String> getProperties() {
    startSmtpServer();
    return Map.of(
        "notification.smtp.host", "localhost",
        "notification.smtp.port", String.valueOf(smtpPort),
        "notification.smtp.from", "store@example.com",
        "notification.smtp.timeout-ms", "3000");
  }

  private static void startSmtpServer() {
    SERVER_LOCK.lock();
    try {
      if (smtpServerSocket != null) {
        return;
      }
      smtpServerSocket = new ServerSocket(0);
      smtpPort = smtpServerSocket.getLocalPort();

      CompletableFuture.runAsync(() -> {
        while (RUNNING.get() && !smtpServerSocket.isClosed()) {
          try {
            Socket clientSocket = smtpServerSocket.accept();
            handleSmtpClient(clientSocket);
          } catch (IOException ignored) {
            // Server socket closed during teardown
          }
        }
      });
    } catch (IOException e) {
      throw new IllegalStateException("Failed to start test SMTP socket server", e);
    } finally {
      SERVER_LOCK.unlock();
    }
  }

  private static void handleSmtpClient(Socket socket) {
    try (socket;
         BufferedReader reader = new BufferedReader(
             new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
         BufferedWriter writer = new BufferedWriter(
             new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

      writer.write("220 localhost Service Ready\r\n");
      writer.flush();

      StringBuilder messageData = new StringBuilder();
      boolean inData = false;
      String line = reader.readLine();

      while (line != null) {
        if (inData) {
          if (CHAR_DOT.equals(line)) {
            inData = false;
            RECEIVED_EMAILS.add(messageData.toString());
            writer.write("250 OK Message Accepted\r\n");
          } else {
            messageData.append(line).append('\n');
          }
        } else {
          inData = handleCommand(writer, line);
          if (CMD_QUIT.equals(line)) {
            break;
          }
        }
        writer.flush();
        line = reader.readLine();
      }
    } catch (IOException ignored) {
      // Client disconnected
    }
  }

  private static boolean handleCommand(BufferedWriter writer, String line) throws IOException {
    if (line.startsWith("HELO") || line.startsWith("EHLO")) {
      writer.write("250 Hello\r\n");
    } else if (line.startsWith("MAIL FROM") || line.startsWith("RCPT TO")) {
      writer.write("250 OK\r\n");
    } else if (CMD_DATA.equals(line)) {
      writer.write("354 Start mail input\r\n");
      return true;
    } else if (CMD_QUIT.equals(line)) {
      writer.write("221 Bye\r\n");
      writer.flush();
    }
    return false;
  }

  @AfterAll
  static void tearDown() {
    RUNNING.set(false);
    if (smtpServerSocket != null && !smtpServerSocket.isClosed()) {
      try {
        smtpServerSocket.close();
      } catch (IOException ignored) {
        // Ignored on close
      }
    }
  }

  @Test
  void orderLifecycleEventsTriggerAsynchronousNotifications(RequestSpecification spec)
      throws InterruptedException {
    UUID orderId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    String customerEmail = "customer-" + orderId + "@example.com";

    String createPayload = buildCreateOrderPayload(orderId, productId, customerEmail);

    int status = given(spec)
        .contentType(ContentType.JSON)
        .body(createPayload)
        .when()
        .post("/v1/orders")
        .then()
        .extract()
        .statusCode();

    assertEquals(201, status);

    String creationEmail = pollEmail(customerEmail, Duration.ofSeconds(10));
    assertNotNull(creationEmail, "Expected email notification for order creation");
    assertTrue(creationEmail.contains("Order Confirmation: " + orderId));
    assertTrue(creationEmail.contains(orderId.toString()));
    assertTrue(creationEmail.contains("123 Delivery Way"));

    String updatePayload = buildUpdateOrderPayload(productId, customerEmail, "PAID");
    given(spec)
        .contentType(ContentType.JSON)
        .body(updatePayload)
        .when()
        .put("/v1/orders/" + orderId)
        .then()
        .statusCode(200);

    String updateEmail = pollEmail(customerEmail, Duration.ofSeconds(10));
    assertNotNull(updateEmail, "Expected email notification for order update");
    assertTrue(updateEmail.contains("Order Update: " + orderId));

    String cancelPayload = buildUpdateOrderPayload(productId, customerEmail, "CANCELLED");
    given(spec)
        .contentType(ContentType.JSON)
        .body(cancelPayload)
        .when()
        .put("/v1/orders/" + orderId)
        .then()
        .statusCode(200);

    String cancellationEmail = pollEmail(customerEmail, Duration.ofSeconds(10));
    assertNotNull(cancellationEmail, "Expected email notification for order cancellation");
    assertTrue(cancellationEmail.contains("Order Cancellation: " + orderId));
  }

  private String pollEmail(String recipient, Duration timeout) throws InterruptedException {
    long deadline = System.currentTimeMillis() + timeout.toMillis();
    while (System.currentTimeMillis() < deadline) {
      String email = RECEIVED_EMAILS.poll(500, TimeUnit.MILLISECONDS);
      if (email != null && email.contains(recipient)) {
        return email;
      }
      if (email != null) {
        RECEIVED_EMAILS.add(email);
      }
    }
    return null;
  }

  private String buildCreateOrderPayload(UUID orderId, UUID productId, String email) {
    return "{"
        + "\"id\":\"" + orderId + "\","
        + "\"customerName\":\"Alice Johnson\","
        + "\"customerEmail\":\"" + email + "\","
        + "\"date\":\"2026-11-11T10:00:00Z\","
        + "\"status\":\"PENDING\","
        + "\"currency\":\"EUR\","
        + "\"deliveryAddress\":{"
        + "\"name\":\"Alice Johnson\","
        + "\"telephone\":\"+123456789\","
        + "\"street\":\"123 Delivery Way\","
        + "\"complement\":\"Suite 200\","
        + "\"city\":\"Springfield\","
        + "\"state\":\"IL\","
        + "\"zipCode\":\"62701\","
        + "\"country\":\"USA\""
        + "},"
        + "\"invoiceAddress\":{"
        + "\"name\":\"Alice Johnson\","
        + "\"vatNumber\":\"US987654321\","
        + "\"telephone\":\"+123456789\","
        + "\"street\":\"123 Delivery Way\","
        + "\"complement\":null,"
        + "\"city\":\"Springfield\","
        + "\"state\":\"IL\","
        + "\"zipCode\":\"62701\","
        + "\"country\":\"USA\""
        + "},"
        + "\"orderLines\":[{"
        + "\"productId\":\"" + productId + "\","
        + "\"quantity\":2,"
        + "\"unitPriceInCents\":1500"
        + "}]"
        + "}";
  }

  private String buildUpdateOrderPayload(
      UUID productId, String email, String status) {
    return "{"
        + "\"customerName\":\"Alice Johnson Updated\","
        + "\"customerEmail\":\"" + email + "\","
        + "\"date\":\"2026-11-11T10:00:00Z\","
        + "\"status\":\"" + status + "\","
        + "\"currency\":\"EUR\","
        + "\"deliveryAddress\":{"
        + "\"name\":\"Alice Johnson\","
        + "\"telephone\":\"+123456789\","
        + "\"street\":\"123 Delivery Way\","
        + "\"complement\":\"Suite 200\","
        + "\"city\":\"Springfield\","
        + "\"state\":\"IL\","
        + "\"zipCode\":\"62701\","
        + "\"country\":\"USA\""
        + "},"
        + "\"invoiceAddress\":{"
        + "\"name\":\"Alice Johnson\","
        + "\"vatNumber\":\"US987654321\","
        + "\"telephone\":\"+123456789\","
        + "\"street\":\"123 Delivery Way\","
        + "\"complement\":null,"
        + "\"city\":\"Springfield\","
        + "\"state\":\"IL\","
        + "\"zipCode\":\"62701\","
        + "\"country\":\"USA\""
        + "},"
        + "\"orderLines\":[{"
        + "\"productId\":\"" + productId + "\","
        + "\"quantity\":3,"
        + "\"unitPriceInCents\":1500"
        + "}]"
        + "}";
  }
}
