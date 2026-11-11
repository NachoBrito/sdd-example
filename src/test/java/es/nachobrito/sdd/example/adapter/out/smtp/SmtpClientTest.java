package es.nachobrito.sdd.example.adapter.out.smtp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;

class SmtpClientTest {

  private static final String CMD_DATA = "DATA";
  private static final String CMD_QUIT = "QUIT";
  private static final String CHAR_DOT = ".";

  @Test
  void sendsEmailSuccessfullyOverSmtpSocket()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      int port = serverSocket.getLocalPort();
      StringBuilder serverReceived = new StringBuilder();

      CompletableFuture<Void> serverTask = CompletableFuture.runAsync(() -> {
        try (Socket socket = serverSocket.accept();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(
                 new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

          writer.write("220-multiline greeting\r\n220 localhost ESMTP Server Ready\r\n");
          writer.flush();

          String line = reader.readLine();
          while (line != null) {
            serverReceived.append(line).append('\n');
            if (line.startsWith("HELO")) {
              writer.write("250-first line\r\n250 Hello\r\n");
            } else if (line.startsWith("MAIL FROM") || line.startsWith("RCPT TO")) {
              writer.write("250 OK\r\n");
            } else if (CMD_DATA.equals(line)) {
              writer.write("354 Start mail input\r\n");
            } else if (CHAR_DOT.equals(line)) {
              writer.write("250\r\n");
            } else if (CMD_QUIT.equals(line)) {
              writer.write("221 Bye\r\n");
              writer.flush();
              break;
            }
            writer.flush();
            line = reader.readLine();
          }
        } catch (IOException e) {
          throw new IllegalStateException("Failed during SMTP test socket handling", e);
        }
      });

      SmtpClient client = new SmtpClient();
      assertDoesNotThrow(() -> client.sendEmail(
          "localhost",
          port,
          2000,
          "sender@example.com",
          "recipient@example.com",
          "Test Subject",
          "<h1>Hello World</h1>"));

      serverTask.get(5, TimeUnit.SECONDS);

      String received = serverReceived.toString();
      assertTrue(received.contains("HELO localhost"));
      assertTrue(received.contains("MAIL FROM:<sender@example.com>"));
      assertTrue(received.contains("RCPT TO:<recipient@example.com>"));
      assertTrue(received.contains("From: sender@example.com\n"));
      assertTrue(received.contains("To: recipient@example.com\n"));
      assertTrue(received.contains("Subject: Test Subject\n"));
      assertTrue(received.contains("MIME-Version: 1.0\n"));
      assertTrue(received.contains("Content-Type: text/html; charset=UTF-8\n"));
      assertTrue(received.contains("\n\n<h1>Hello World</h1>"));
      assertTrue(received.contains("QUIT"));
    }
  }

  @Test
  void throwsIoExceptionWhenServerRespondsWithErrorCode() throws IOException {
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      int port = serverSocket.getLocalPort();

      CompletableFuture.runAsync(() -> {
        try (Socket socket = serverSocket.accept();
             BufferedWriter writer = new BufferedWriter(
                 new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
          writer.write("500 Server Error\r\n");
          writer.flush();
        } catch (IOException ignored) {
          // ignore
        }
      });

      SmtpClient client = new SmtpClient();
      assertThrows(IOException.class, () -> client.sendEmail(
          "localhost",
          port,
          2000,
          "sender@example.com",
          "recipient@example.com",
          "Subject",
          "Body"));
    }
  }

  @Test
  void throwsIoExceptionWhenServerRejectsQuit() throws IOException {
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      int port = serverSocket.getLocalPort();

      CompletableFuture.runAsync(() -> {
        try (Socket socket = serverSocket.accept();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(
                 new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

          writer.write("220 Ready\r\n");
          writer.flush();

          String line = reader.readLine();
          while (line != null) {
            if (line.startsWith("HELO")) {
              writer.write("250 Hello\r\n");
            } else if (line.startsWith("MAIL FROM") || line.startsWith("RCPT TO")) {
              writer.write("250 OK\r\n");
            } else if (CMD_DATA.equals(line)) {
              writer.write("354 Start\r\n");
            } else if (CHAR_DOT.equals(line)) {
              writer.write("250 OK\r\n");
            } else if (CMD_QUIT.equals(line)) {
              writer.write("500 Quit Rejected\r\n");
              writer.flush();
              break;
            }
            writer.flush();
            line = reader.readLine();
          }
        } catch (IOException ignored) {
          // ignore
        }
      });

      SmtpClient client = new SmtpClient();
      assertThrows(IOException.class, () -> client.sendEmail(
          "localhost",
          port,
          2000,
          "sender@example.com",
          "recipient@example.com",
          "Subject",
          "Body"));
    }
  }

  @Test
  void throwsIoExceptionWhenStreamClosesPrematurely() throws IOException {
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      int port = serverSocket.getLocalPort();

      CompletableFuture.runAsync(() -> {
        try {
          Socket socket = serverSocket.accept();
          socket.close();
        } catch (IOException ignored) {
          // ignore
        }
      });

      SmtpClient client = new SmtpClient();
      assertThrows(IOException.class, () -> client.sendEmail(
          "localhost",
          port,
          2000,
          "sender@example.com",
          "recipient@example.com",
          "Subject",
          "Body"));
    }
  }
}
