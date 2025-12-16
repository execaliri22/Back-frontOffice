import { Component, Input, Output, EventEmitter, inject, signal, computed, OnInit } from '@angular/core';
import { Producto } from '../../core/models/models';
import { CarritoService } from '../../core/services/carrito.service';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { FavoritoService } from '../../core/services/favorito.service';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './product-card.component.html',
  styleUrls: ['./product-card.component.css']
})
export class ProductCardComponent implements OnInit {
  @Input({ required: true }) producto!: Producto;
  
  // Evento para avisar al padre que muestre la notificación
  @Output() notificar = new EventEmitter<string>(); 

  private carritoService = inject(CarritoService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private favoritoService = inject(FavoritoService);

  public agregando = signal(false);
  public errorAgregar: string | null = null;
  public procesandoFavorito = signal(false);

  // El computed leerá el estado ACTUALIZADO del servicio
  public esFavorito = computed(() =>
    this.producto ? this.favoritoService.esFavorito(this.producto.idProducto) : false
  );

  ngOnInit(): void {}

  agregarAlCarrito(): void {
    if (!this.authService.isLoggedIn()) {
      alert('Debes iniciar sesión para agregar productos al carrito.');
      this.router.navigate(['/auth']);
      return;
    }

    this.agregando.set(true);
    this.errorAgregar = null;

    this.carritoService.agregarItem(this.producto.idProducto, 1).subscribe({
      next: () => {
        this.agregando.set(false);
        // EMITIMOS EL MENSAJE AL PADRE
        this.notificar.emit(`🛒 ${this.producto.nombre} añadido al carrito`);
      },
      error: (err) => {
        this.errorAgregar = err.message || 'Error al añadir el producto.';
        this.agregando.set(false);
      }
    });
  }

  toggleFavorito(): void {
    if (!this.authService.isLoggedIn()) {
      alert('Debes iniciar sesión para gestionar tus favoritos.');
      this.router.navigate(['/auth']);
      return;
    }

    this.procesandoFavorito.set(true);
    const currentlyFavorite = this.esFavorito();
    const productId = this.producto.idProducto;

    const request$ = currentlyFavorite
      ? this.favoritoService.eliminarFavorito(productId)
      : this.favoritoService.agregarFavorito(productId);

    request$.subscribe({
      next: () => {
        // La actualización visual ocurre AQUÍ porque el 'tap' del servicio ya modificó el Signal.
        
        this.procesandoFavorito.set(false);
        
        // EMITIMOS EL MENSAJE AL PADRE
        const emoji = currentlyFavorite ? '💔' : '⭐'; // Usa el estado ANTES del cambio
        const actionText = currentlyFavorite ? 'eliminado de' : 'agregado a';
        this.notificar.emit(`${emoji} ${this.producto.nombre} ${actionText} favoritos`);
      },
      error: (err) => {
        console.error('Error favoritos:', err);
        this.procesandoFavorito.set(false);
        this.notificar.emit(`❌ Error al modificar favoritos.`);
      }
    });
  }
}