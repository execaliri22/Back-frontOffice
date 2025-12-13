import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common'; 
import { RouterLink } from '@angular/router';   
import { Observable } from 'rxjs';

import { CarritoService } from '../../core/services/carrito.service';
import { Carrito, ItemCarrito } from '../../core/models/models';

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
  // Eliminado: PedidoService (ya no se usa aquí)
  // Eliminado: Router (usamos routerLink en el HTML)

  // --- PROPIEDADES ---
  public carrito$: Observable<Carrito | null> = this.carritoService.carrito$;
  public error: string | null = null;
  // Eliminado: procesandoCompra (ya no hay espera asíncrona aquí)

  ngOnInit(): void {
    // El servicio se encarga de la carga inicial
  }

  // --- MÉTODOS ---

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
}