import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common'; // Para *ngIf, *ngFor, | async, | currency
import { RouterLink, Router } from '@angular/router';   // Importamos Router
import { Observable } from 'rxjs';

// Servicios y Modelos
import { CarritoService } from '../../core/services/carrito.service';
import { PedidoService } from '../../core/services/pedido.service'; 
// Importamos los modelos necesarios, incluyendo Pedido
import { Carrito, ItemCarrito, Pedido } from '../../core/models/models';

@Component({
  selector: 'app-carrito',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink
  ],
  templateUrl: './carrito.component.html',
  styleUrls: ['./carrito.component.css']
})
export class CarritoComponent implements OnInit {

  // --- INYECCIONES ---
  private carritoService = inject(CarritoService);
  private pedidoService = inject(PedidoService);
  private router = inject(Router);

  // --- PROPIEDADES ---
  public carrito$: Observable<Carrito | null> = this.carritoService.carrito$;
  public error: string | null = null;
  public procesandoCompra = false; // Para deshabilitar el botón mientras carga

  ngOnInit(): void {
    // El servicio se encarga de la carga inicial
  }

  // --- MÉTODOS EXISTENTES (Eliminar / Actualizar) ---

  eliminarItem(itemId: number): void {
     this.error = null; 
     this.carritoService.eliminarItem(itemId).subscribe({
       next: () => console.log(`Item ${itemId} eliminado`),
       error: (err) => {
         console.error(err);
         this.error = 'Error al eliminar producto.';
       }
     });
  }

  actualizarCantidad(item: ItemCarrito, nuevaCantidadStr: string): void {
    this.error = null; 
    const nuevaCantidad = parseInt(nuevaCantidadStr, 10);

    if (isNaN(nuevaCantidad) || nuevaCantidad < 1) {
        this.error = 'Cantidad inválida.';
        return;
    }

    this.carritoService.actualizarCantidad(item.id, nuevaCantidad).subscribe({
      next: () => console.log(`Cantidad actualizada`),
      error: (err) => {
        console.error(err);
        this.error = 'Error al actualizar cantidad.';
      }
    });
  }

  calcularTotal(items: ItemCarrito[] | undefined | null): number {
    if (!items) return 0;
    return items.reduce((total, item) => total + item.subtotal, 0);
  }

  // --- NUEVO MÉTODO: FINALIZAR COMPRA (CHECKOUT) ---
  
  finalizarCompra() {
    if(!confirm('¿Estás seguro de confirmar tu compra?')) return;

    this.procesandoCompra = true; // Activa loading (bloquea botón)
    this.error = null;

    this.pedidoService.crearPedido().subscribe({
      // 👇 AQUÍ ESTÁ EL CAMBIO IMPORTANTE: Agregamos ": Pedido"
      next: (pedido: Pedido) => {
        this.procesandoCompra = false;
        
        // 1. Avisar al usuario
        alert(`¡Compra Exitosa! Pedido #${pedido.idPedido} generado.`);

        // 2. Redirigir a "Mis Pedidos"
        this.router.navigate(['/mis-pedidos']);
      },
      error: (e) => {
        this.procesandoCompra = false;
        console.error('Error en checkout:', e);
        // Muestra el mensaje que venga del backend (ej: "Sin stock") o uno genérico
        this.error = e.error || 'Ocurrió un error al procesar tu compra. Intenta nuevamente.';
      }
    });
  }
}