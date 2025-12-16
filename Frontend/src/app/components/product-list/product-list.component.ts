import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Producto } from '../../core/models/models';
import { ProductCardComponent } from "../product-card/product-card.component";

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, ProductCardComponent],
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent {
  @Input() productos: Producto[] = [];
  
  // Variables para la notificación (Toast)
  mostrarNotificacion = false;
  mensajeNotificacion = '';
  timeoutId: any;

  // Este método es llamado por el hijo (ProductCard) a través del evento (notificar)
  mostrarMensaje(mensaje: string) {
    this.mensajeNotificacion = mensaje;
    this.mostrarNotificacion = true;

    if (this.timeoutId) clearTimeout(this.timeoutId);

    this.timeoutId = setTimeout(() => {
      this.mostrarNotificacion = false;
    }, 3000);
  }
}