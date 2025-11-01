package com.example.front_office.service;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaypalService {

    private final APIContext apiContext;

    // Inyectamos el APIContext configurado en PaypalConfig
    @Autowired
    public PaypalService(APIContext apiContext) {
        this.apiContext = apiContext;
    }

    /**
     * Crea una orden de pago en PayPal y devuelve el objeto Payment
     */
    public Payment createPayment(
            Double total,
            String currency,
            String method,          // ej. "paypal"
            String intent,          // ej. "sale" o "authorize"
            String description,
            String cancelUrl,
            String successUrl) throws PayPalRESTException {

        // 1. Configuración del Monto
        Amount amount = new Amount();
        amount.setCurrency(currency);
        // Formatear el total a dos decimales
        total = new BigDecimal(total).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        amount.setTotal(String.format("%.2f", total)); // Formato de string requerido por PayPal

        // 2. Configuración de la Transacción (lo que se va a cobrar)
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        // 3. Configuración del Pago
        Payment payment = new Payment();
        payment.setIntent(intent);
        payment.setTransactions(transactions);

        // 4. Configuración del Payer (el método de pago)
        Payer payer = new Payer();
        payer.setPaymentMethod(method);
        payment.setPayer(payer);

        // 5. URLs de Redirección después de la aprobación/cancelación
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);

        // Llamada a la API de PayPal para crear la orden
        return payment.create(apiContext);
    }

    /**
     * Ejecuta el pago final después de que el cliente lo aprueba.
     */
    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);

        // Obtiene el objeto Payment
        Payment payment = new Payment();
        payment.setId(paymentId);

        // Ejecuta la captura final del pago
        return payment.execute(apiContext, paymentExecution);
    }
}