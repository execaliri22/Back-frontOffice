import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PedidoService } from '../../core/services/pedido.service';
import { Pedido } from '../../core/models/models'; // Importamos el modelo

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})
export class CheckoutComponent {

  private pedidoService = inject(PedidoService);
  private router = inject(Router);

  error: string | null = null;
  procesando = false;

  // Este método reemplaza tu antigua lógica de "procesarPago"
  confirmarPago() {
    this.procesando = true;
    this.error = null;

    // Usamos el método REAL que creamos en el servicio
    this.pedidoService.crearPedido().subscribe({
      next: (pedido: Pedido) => { // Tipamos explícitamente para evitar error TS7006
        this.procesando = false;
        alert(`¡Pago procesado con éxito! Pedido #${pedido.idPedido}`);
        this.router.navigate(['/mis-pedidos']);
      },
      error: (err: any) => { // Tipamos el error como any
        this.procesando = false;
        console.error(err);
        this.error = 'Error al procesar el pedido. Intente nuevamente.';
      }
    });
  }
}