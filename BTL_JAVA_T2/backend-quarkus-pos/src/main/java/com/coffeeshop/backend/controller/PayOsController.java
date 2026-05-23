package com.coffeeshop.backend.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller xử lý thanh toán PayOS.
 * Base path: /api/payments/payos
 *
 * Endpoints:
 * POST /create - Tạo link thanh toán, trả về checkoutUrl + qrCode.
 * POST /webhook - Nhận webhook từ PayOS, xác thực chữ ký.
 *
 * ─── SDK v2.0.1 API ─────────────────────────────────────────────────────────
 * Tạo payment link:
 * payOS.paymentRequests().create(CreatePaymentLinkRequest) →
 * CreatePaymentLinkResponse
 *
 * Xác thực webhook:
 * payOS.webhooks().verify(Webhook body) → WebhookData
 *
 * Package models:
 * vn.payos.model.v2.paymentRequests.* (payment link)
 * vn.payos.model.webhooks.* (webhook)
 * ────────────────────────────────────────────────────────────────────────────
 */
@Path("/api/payments/payos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PayOsController {

    private static final Logger log = LoggerFactory.getLogger(PayOsController.class);

    @Inject
    PayOS payOS;

    // -------------------------------------------------------------------------
    // POST /api/payments/payos/create
    // Body: { "orderCode": 123456, "amount": 50000, "description": "Thanh toan" }
    // -------------------------------------------------------------------------
    @POST
    @Path("/create")
    public Response createPaymentLink(CreatePaymentRequest request) {
        try {
            log.info("Tao link thanh toan PayOS cho don hang #{}, so tien: {} VND",
                    request.getOrderCode(), request.getAmount());

            // SDK v2: builder dùng Long cho orderCode và amount
            CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                    .orderCode(request.getOrderCode())
                    .amount(request.getAmount())
                    .description(request.getDescription())
                    .returnUrl("http://localhost:8081/payment/success")
                    .cancelUrl("http://localhost:8081/payment/cancel")
                    .build();

            // SDK v2: payOS.paymentRequests().create(...)
            CreatePaymentLinkResponse response = payOS.paymentRequests().create(paymentData);

            log.info("Tao link thanh toan thanh cong. checkoutUrl={}, qrCode={}",
                    response.getCheckoutUrl(), response.getQrCode());

            // Trả về checkoutUrl và qrCode cho Swing client
            Map<String, String> body = new HashMap<>();
            body.put("checkoutUrl", response.getCheckoutUrl());
            body.put("qrCode", response.getQrCode());
            body.put("orderCode", String.valueOf(response.getOrderCode()));

            return Response.ok(body).build();

        } catch (Exception e) {
            log.error("Loi khi tao link thanh toan PayOS: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    // -------------------------------------------------------------------------
    // POST /api/payments/payos/webhook
    // Body: JSON payload PayOS gửi – Jackson tự deserialise vào Webhook.
    // {
    // "code": "00", "desc": "success", "success": true,
    // "data": { "orderCode": ..., "amount": ..., ... },
    // "signature": "..."
    // }
    // -------------------------------------------------------------------------
    @POST
    @Path("/webhook")
    public Response handleWebhook(Webhook webhookPayload) {
        try {
            log.info("Nhan webhook tu PayOS, orderCode: {}",
                    webhookPayload.getData() != null ? webhookPayload.getData().getOrderCode() : "N/A");

            // SDK v2: payOS.webhooks().verify(Object) → WebhookData
            // Truyền thẳng đối tượng Webhook đã được Jackson parse sẵn.
            WebhookData webhookData = payOS.webhooks().verify(webhookPayload);

            long orderCode = webhookData.getOrderCode();

            // In ra console đúng như yêu cầu
            System.out.println("Thanh toan thanh cong cho don hang: " + orderCode);
            log.info("Thanh toan thanh cong cho don hang: {}", orderCode);

            return Response.ok(Map.of("message", "Webhook xu ly thanh cong")).build();

        } catch (Exception e) {
            log.error("Loi xac thuc webhook PayOS: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Xac thuc webhook that bai: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
    }

    // Kéo cấu hình từ file application.yml vào để gọi API trực tiếp
    @org.eclipse.microprofile.config.inject.ConfigProperty(name = "payos.client-id")
    String clientId;

    @org.eclipse.microprofile.config.inject.ConfigProperty(name = "payos.api-key")
    String apiKey;

    @GET
    @Path("/status/{orderCode}")
    public Response checkPaymentStatus(@PathParam("orderCode") long orderCode) {
        try {
            // Không thèm dùng SDK nữa, gọi thẳng API gốc của PayOS!
            java.net.URL url = new java.net.URL("https://api-merchant.payos.vn/v2/payment-requests/" + orderCode);
            java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("x-client-id", clientId);
            con.setRequestProperty("x-api-key", apiKey);

            // Đọc JSON PayOS trả về
            java.util.Scanner scanner = new java.util.Scanner(con.getInputStream());
            String responseStr = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";

            // Dùng Regex lấy chính xác chữ PAID hoặc PENDING
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"status\"\\s*:\\s*\"([^\"]+)\"")
                    .matcher(responseStr);
            String status = matcher.find() ? matcher.group(1) : "PENDING";

            return Response.ok("{\"status\":\"" + status + "\"}").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(400).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    // -------------------------------------------------------------------------
    // Inner DTO – request body cho endpoint /create
    // (Long để tương thích với CreatePaymentLinkRequest.builder())
    // -------------------------------------------------------------------------
    public static class CreatePaymentRequest {
        private Long orderCode;
        private Long amount;
        private String description;

        public Long getOrderCode() {
            return orderCode;
        }

        public Long getAmount() {
            return amount;
        }

        public String getDescription() {
            return description;
        }

        public void setOrderCode(Long orderCode) {
            this.orderCode = orderCode;
        }

        public void setAmount(Long amount) {
            this.amount = amount;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
