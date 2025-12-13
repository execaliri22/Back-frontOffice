package com.example.front_office.service;



import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.preference.Preference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    public String crearPreferencia() {
        MercadoPagoConfig.setAccessToken("APP_USR-5074996650738682-121308-b5298230a446a3f5704ff5c654ad7a45-3060739144");

        try {
            // 1. Ítem
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title("Compra en Feel Store")
                    .quantity(1)
                    .unitPrice(new BigDecimal("100.00"))
                    .currencyId("ARS")
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);

            // 2. URL (Creamos el objeto)
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("http://localhost:4200/pago/exitoso")
                    .pending("http://localhost:4200/pago/pendiente")
                    .failure("http://localhost:4200/pago/fallo")
                    .build();

            // 3. Preferencia (¡AQUÍ ES DONDE SUELE FALTAR LA LÍNEA!)
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    // .autoReturn("approved") // Desactívalo si te da problemas por ahora
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // CLAVE: Para probar en el entorno "real", muchos usan getInitPoint().
            // Sin embargo, getSandboxInitPoint() sigue existiendo para forzar la vista de pruebas.
            // Si quieres la experiencia "real" con credenciales ficticias, usa init_point.
            return preference.getInitPoint();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}