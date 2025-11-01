package com.example.front_office.controller;

import com.example.front_office.service.PaypalService;
import com.example.front_office.controller.dto.OrderRequest; // <--- ¡IMPORT CORREGIDO!
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
public class PaypalPaymentController {

    private final PaypalService paypalService;
    public static final String FRONTEND_URL = "http://localhost:8080";

    @Autowired
    public PaypalPaymentController(PaypalService paypalService) {
        this.paypalService = paypalService;
    }

    // --- 1. ENDPOINT: Crea la Orden usando el DTO ---
    @PostMapping("/api/paypal/create-order")
    // Spring Boot ahora deserializará el JSON del body en el OrderRequest DTO
    public ResponseEntity<Map<String, String>> createOrder(@RequestBody OrderRequest request) {

        Double total = request.getTotal();
        String currency = request.getCurrency();

        try {
            String cancelUrl = FRONTEND_URL + "/pago/cancel";
            String successUrl = FRONTEND_URL + "/pago/success";

            Payment payment = paypalService.createPayment(
                    total,
                    currency != null ? currency : "USD",
                    "paypal",
                    "sale",
                    "Compra de productos en e-commerce",
                    cancelUrl,
                    successUrl
            );

            // Búsqueda de la URL de aprobación
            String approvalUrl = null;
            for(Links link: payment.getLinks()){
                if(link.getRel().equals("approval_url")){
                    approvalUrl = link.getHref();
                    break;
                }
            }

            if (approvalUrl != null) {
                Map<String, String> response = new HashMap<>();
                response.put("approval_url", approvalUrl);

                return ResponseEntity.ok(response);
            }

        } catch (PayPalRESTException e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al crear la orden de PayPal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Error inesperado al obtener la URL de aprobación.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // --- 2. ENDPOINT: Captura el Pago (Queda igual) ---
    @GetMapping("/pago/success")
    public String successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);

            if(payment.getState().equals("approved")){
                // LÓGICA DE NEGOCIO CRÍTICA: Actualizar DB, enviar correo, etc.
                return "redirect:" + FRONTEND_URL + "/pago/confirmado";
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }
        return "redirect:" + FRONTEND_URL + "/pago/error";
    }
}