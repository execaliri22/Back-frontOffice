import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CarritoService } from '../../core/services/carrito.service';
import { PedidoService } from '../../core/services/pedido.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})
export class CheckoutComponent implements OnInit {

  private carritoService = inject(CarritoService);
  private pedidoService = inject(PedidoService);
  private http = inject(HttpClient);
  private router = inject(Router);
  private authService = inject(AuthService);

  // --- VARIABLES QUE PIDE EL HTML ---
  carrito$: any;
  total$: any;
  usuario: any = null;
  
  // Cambiamos 'loading' por 'procesando' como pide tu HTML
  procesando = false; 
  // Agregamos la variable 'error' que pide tu HTML
  error: string | null = null; 

  ngOnInit() {
    this.carrito$ = this.carritoService.carrito$;
    this.total$ = this.carritoService.total$;
    this.usuario = this.authService.getCurrentUser();
  }

  // Cambiamos 'realizarPago' por 'confirmarPago' como pide tu HTML
  confirmarPago() {
    if (this.procesando) return;
    this.procesando = true;
    this.error = null; // Limpiamos errores previos

    // 1. Crear Pedido
    this.pedidoService.crearPedido().subscribe({
      next: (pedidoCreado: any) => {
        console.log('Pedido creado:', pedidoCreado);
        
        if (pedidoCreado && pedidoCreado.idPedido) {
             this.iniciarMercadoPago(pedidoCreado.idPedido);
        } else {
             this.error = 'El pedido se creó pero no tiene ID.';
             this.procesando = false;
        }
      },
      error: (err) => {
        console.error('Error creando pedido:', err);
        this.error = 'Hubo un error al procesar el pedido. Intenta nuevamente.';
        this.procesando = false;
      }
    });
  }

  private iniciarMercadoPago(idPedido: number) {
    this.http.post<any>(`http://localhost:8080/api/pagos/crear_preferencia/${idPedido}`, {})
      .subscribe({
        next: (res) => {
          if (res && res.url) {
            // Redirigir a Mercado Pago
            window.location.href = res.url;
          } else {
            this.error = 'El servidor no devolvió el link de pago.';
            this.procesando = false;
          }
        },
        error: (err) => {
          console.error('Error HTTP:', err);
          this.error = 'Error de conexión con el servidor de pagos.';
          this.procesando = false;
        }
      });
  }
}