package com.example.front_office.controller;

import com.example.front_office.model.Carrito;
import com.example.front_office.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

record AddItemRequest(Integer idProducto, int cantidad) {}

@RestController
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired private CarritoService carritoService;

    @SuppressWarnings("rawtypes")
    @PostMapping
    public ResponseEntity<Carrito> agregarAlCarrito(@RequestBody AddItemRequest request) {
        // NOTA: En una app real, el idUsuario se obtendría del token de seguridad.
        // Por simplicidad, lo hardcodeamos a 1.
        Integer idUsuario = 1;
        Carrito carrito = carritoService.agregarItem(idUsuario, request.idProducto(), request.cantidad());
        return ResponseEntity.ok(carrito);
    }
}