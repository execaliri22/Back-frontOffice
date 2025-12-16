package com.example.front_office.service;

import com.example.front_office.model.Pedido;
import com.example.front_office.model.ItemPedido;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    // --- AQUÍ ESTÁ EL CAMBIO IMPORTANTE ---
    // Agregamos (Pedido pedido) entre los paréntesis
    public String crearPreferencia(Pedido pedido) {

        // Tu Token
        MercadoPagoConfig.setAccessToken("APP_USR-5074996650738682-121308-b5298230a446a3f5704ff5c654ad7a45-3060739144");

        try {
            List<PreferenceItemRequest> items = new ArrayList<>();

            // Recorremos los items REALES del pedido
            for (ItemPedido item : pedido.getItems()) {

                // Validación de precio (por si es nulo)
                BigDecimal precio = item.getProducto().getPrecio();
                if (precio == null) precio = new BigDecimal("1.00");

                PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                        .title(item.getProducto().getNombre())
                        .pictureUrl(item.getProducto().getUrlImagen())
                        .quantity(item.getCantidad())
                        .currencyId("ARS")
                        .unitPrice(precio)
                        .build();

                items.add(itemRequest);
            }

            // URLs apuntando a tu Backend (8080)
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("http://localhost:8080/api/pagos/success")
                    .pending("http://localhost:8080/api/pagos/pending")
                    .failure("http://localhost:8080/api/pagos/failure")
                    .build();

            // Creación de la Preferencia con Referencia Externa
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    // .autoReturn("approved") // Desactivado para localhost
                    .externalReference(String.valueOf(pedido.getIdPedido())) // CLAVE: Enviamos el ID
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            return preference.getInitPoint();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}