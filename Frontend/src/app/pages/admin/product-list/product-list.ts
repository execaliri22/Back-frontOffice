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
  borrarProducto(id: number) {
    if(confirm('¿Borrar producto?')) {
      this.adminService.deleteProducto(id).subscribe(() => this.cargarProductos());
    }
  }
}