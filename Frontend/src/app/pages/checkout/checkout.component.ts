import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css'] // Asegúrate que el archivo css exista
})
export class CheckoutComponent {
  
  private http = inject(HttpClient);
  private router = inject(Router);

  // Apuntamos al Backend LOCAL
  private baseUrl = 'http://localhost:8080/api'; 
  
  public procesando = false;
  public error: string = '';

  pagar() {
    this.procesando = true;
    this.error = '';

    // 1. Crear el pedido en BD (Limpia carrito y descuenta stock)
    this.http.post(`${this.baseUrl}/pedidos`, {}).subscribe({
      next: (pedido: any) => {
        console.log('Pedido creado:', pedido);
        // 2. Si se crea bien, pedimos el link de Mercado Pago
        this.iniciarPagoMercadoPago(pedido.idPedido);
      },
      error: (err) => {
        console.error(err);
        this.error = 'Error al crear el pedido. Revisa el stock o tu carrito.';
        this.procesando = false;
      }
    });
  }

  iniciarPagoMercadoPago(idPedido: number) {
    this.http.post(`${this.baseUrl}/pedidos/${idPedido}/pagar`, {}).subscribe({
      next: (res: any) => {
        // 3. Redirigir a Mercado Pago
        window.location.href = res.url;
      },
      error: (err) => {
        console.error('Error al generar link MP', err);
        this.error = 'Error al conectar con Mercado Pago.';
        this.procesando = false;
      }
    });
  }
}