import { Component, Input, inject, signal, computed, OnInit } from '@angular/core';
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

  private carritoService = inject(CarritoService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private favoritoService = inject(FavoritoService);

  public agregando = signal(false);
  public errorAgregar: string | null = null;
  
  // Signals para Favoritos y Toast
  public procesandoFavorito = signal(false);
  public mostrarToast = signal(false);
  public mensajeToast = signal('');

  // Computed signal que reacciona a los cambios en el servicio
  public esFavorito = computed(() =>
    this.producto ? this.favoritoService.esFavorito(this.producto.idProducto) : false
  );

  ngOnInit(): void {}

  agregarAlCarrito(): void {
    if (!this.authService.isLoggedIn()) {
      alert('Debes iniciar sesión para comprar.');
      this.router.navigate(['/auth']);
      return;
    }

    this.agregando.set(true);
    this.errorAgregar = null;

    this.carritoService.agregarItem(this.producto.idProducto, 1).subscribe({
      next: () => {
        this.agregando.set(false);
        this.mensajeToast.set('Agregado al carrito 🛒');
        this.mostrarToast.set(true);
        setTimeout(() => this.mostrarToast.set(false), 2000);
      },
      error: (err) => {
        console.error('Error al añadir:', err);
        this.errorAgregar = 'Error al añadir.';
        this.agregando.set(false);
      }
    });
  }

  toggleFavorito(): void {
    if (!this.authService.isLoggedIn()) {
      alert('Inicia sesión para guardar favoritos.');
      this.router.navigate(['/auth']);
      return;
    }

    this.procesandoFavorito.set(true);
    
    // Guardamos el estado ACTUAL antes de llamar a la API
    const currentlyFavorite = this.esFavorito();
    const productId = this.producto.idProducto;

    const request$ = currentlyFavorite
      ? this.favoritoService.eliminarFavorito(productId)
      : this.favoritoService.agregarFavorito(productId);

    request$.subscribe({
      next: () => {
        this.procesandoFavorito.set(false);
        
        // CORRECCIÓN LÓGICA:
        // Si ERA favorito (true), ahora lo hemos eliminado -> Mensaje "Eliminado"
        // Si NO ERA favorito (false), ahora lo agregamos -> Mensaje "Añadido"
        this.mensajeToast.set(currentlyFavorite ? 'Eliminado de Favoritos 💔' : '¡Añadido a Favoritos! ⭐');
        
        this.mostrarToast.set(true);
        
        setTimeout(() => {
          this.mostrarToast.set(false);
        }, 2000);
      },
      error: (err) => {
        console.error('Error fav:', err);
        this.procesandoFavorito.set(false);
      }
    });
  }
}