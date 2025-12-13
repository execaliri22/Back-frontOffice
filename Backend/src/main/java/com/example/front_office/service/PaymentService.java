package com.example.front_office.service;

import com.example.front_office.model.ItemPedido;
import com.example.front_office.model.Pedido;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.resources.preference.Preference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    public String crearPreferencia(Pedido pedido) {
        // Tu Access Token de Vendedor de Prueba
        MercadoPagoConfig.setAccessToken("APP_USR-5074996650738682-121308-b5298230a446a3f5704ff5c654ad7a45-3060739144");

        try {
            // 1. Crear lista de ítems basada en el pedido real
            List<PreferenceItemRequest> items = new ArrayList<>();

            for (ItemPedido itemP : pedido.getItems()) {
                PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                        .title(itemP.getProducto().getNombre())
                        .quantity(itemP.getCantidad())
                        .unitPrice(itemP.getProducto().getPrecio()) // Precio real de la BD
                        .currencyId("ARS")
                        .build();
                items.add(itemRequest);
            }

            // 2. URLs de retorno apuntando a tu Angular LOCAL (Puerto 4200)
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("http://localhost:4200/pago/exitoso")
                    .pending("http://localhost:4200/pago/pendiente")
                    .failure("http://localhost:4200/pago/fallo")
                    .build();

            // 3. Crear la preferencia con ID de pedido y auto-retorno
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    //.autoReturn("approved") // Vuelve automático si es exitoso
                    .externalReference(String.valueOf(pedido.getIdPedido())) // ID del pedido para identificarlo al volver
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