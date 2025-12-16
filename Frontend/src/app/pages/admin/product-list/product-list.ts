import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common'; // <--- SOLUCIONA NG8103 (*ngIf, *ngFor)
import { RouterLink } from '@angular/router';   // <--- SOLUCIONA NG8002 (routerLink)
import { AdminService } from '../../../core/services/admin.service';
import { Producto } from '../../../core/models/models';

@Component({
  selector: 'app-admin-product-list',
  standalone: true,
  imports: [CommonModule, RouterLink], // <--- IMPORTANTE
  templateUrl: './product-list.html',   // Asegúrate que tu archivo HTML se llame así
  styleUrls: ['./product-list.css']     // Asegúrate que tu archivo CSS se llame así
})
// SOLUCIONA TS2305: Cambiamos el nombre a ProductListComponent
export class ProductListComponent implements OnInit { 

  private adminService = inject(AdminService);
  
  productos: Producto[] = [];
  loading = true;

  ngOnInit(): void {
    this.cargarProductos();
  }

  cargarProductos() {
    this.loading = true;
    this.adminService.getProductos().subscribe({
      next: (data) => {
        this.productos = data;
        this.loading = false;
      },
      error: (e) => {
        console.error(e);
        this.loading = false;
      }
    });
  }

  // SOLUCIONA TS2339: Agregamos el método que faltaba
borrarProducto(idProducto: number): void {
  if (confirm(`¿Estás seguro de desactivar el Producto #${idProducto}?`)) {
    this.adminService.deleteProducto(idProducto).subscribe({
      next: () => {
        // Soft Delete exitoso
        alert(`Producto #${idProducto} desactivado (Soft Delete).`);
        
        // ***************************************************
        // CRÍTICO: FILTRAR LA LISTA LOCAL PARA ACTUALIZAR LA VISTA
        // ***************************************************
        this.productos = this.productos.filter(p => p.idProducto !== idProducto);
        
        // Opcional: Si el producto tiene una propiedad 'activo', la podrías cambiar
        // const productoDesactivado = this.productos.find(p => p.idProducto === idProducto);
        // if (productoDesactivado) {
        //     productoDesactivado.activo = false;
        // }
        // Pero el filtrado es más limpio si no quieres ver los inactivos.
        
      },
      error: (err) => {
        console.error('Error al desactivar:', err);
        alert('Error al desactivar. Verifica la conexión o los permisos de ADMIN.');
      }
    });
  }
}
}