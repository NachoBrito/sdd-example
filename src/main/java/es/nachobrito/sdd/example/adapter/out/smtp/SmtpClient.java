package es.nachobrito.sdd.example.adapter.out.smtp;

import jakarta.inject.Singleton;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Lightweight SMTP client using standard JDK networking.
 */
@Singleton
public class SmtpClient {

  private static final int STATUS_CODE_LENGTH = 3;

  /**
   * Sends an email via SMTP.
   *
   * @param host SMTP server hostname
   * @param port SMTP server port
   * @param timeoutMs connection and read timeout in milliseconds
   * @param from sender email address
   * @param to recipient email address
   * @param subject email subject line
   * @param htmlBody HTML body content
   * @throws IOException when network or protocol errors occur
   */
  public void sendEmail(
      String host,
      int port,
      int timeoutMs,
      String from,
      String to,
      String subject,
      String htmlBody) throws IOException {

    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(host, port), timeoutMs);
      socket.setSoTimeout(timeoutMs);

      try (BufferedReader reader = new BufferedReader(
              new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
           BufferedWriter writer = new BufferedWriter(
               new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

        readResponse(reader, 220);

        sendCommand(writer, "HELO localhost");
        readResponse(reader, 250);

        sendCommand(writer, "MAIL FROM:<" + from + ">");
        readResponse(reader, 250);

        sendCommand(writer, "RCPT TO:<" + to + ">");
        readResponse(reader, 250);

        sendCommand(writer, "DATA");
        readResponse(reader, 354);

        writer.write("From: " + from + "\r\n");
        writer.write("To: " + to + "\r\n");
        writer.write("Subject: " + subject + "\r\n");
        writer.write("MIME-Version: 1.0\r\n");
        writer.write("Content-Type: text/html; charset=UTF-8\r\n\r\n");
        writer.write(htmlBody);
        writer.write("\r\n.\r\n");
        writer.flush();
        readResponse(reader, 250);

        sendCommand(writer, "QUIT");
        readResponse(reader, 221);
      }
    }
  }

  private void sendCommand(BufferedWriter writer, String command) throws IOException {
    writer.write(command + "\r\n");
    writer.flush();
  }

  private void readResponse(BufferedReader reader, int expectedPrefix) throws IOException {
    String line = reader.readLine();
    while (line != null) {
      if (line.length() >= STATUS_CODE_LENGTH) {
        int code = Integer.parseInt(line.substring(0, STATUS_CODE_LENGTH));
        boolean terminal = line.length() == STATUS_CODE_LENGTH
            || line.charAt(STATUS_CODE_LENGTH) == ' ';
        if (terminal && code != expectedPrefix) {
          throw new IOException("Unexpected SMTP response: " + line);
        }
        if (terminal) {
          return;
        }
      }
      line = reader.readLine();
    }
    throw new IOException("Unexpected end of stream while waiting for SMTP response");
  }
}
