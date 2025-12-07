import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms'; // Necesario para [(ngModel)]
import { CategoriaService } from '../../../core/services/categoria.service';
import { AdminService } from '../../../core/services/admin.service';
import { Categoria } from '../../../core/models/models';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './category-list.html',   // <--- Ajustado a tu convención
  styleUrls: ['./category-list.css']     // <--- Ajustado a tu convención
})
export class CategoryListComponent implements OnInit {
  
  private categoriaService = inject(CategoriaService);
  private adminService = inject(AdminService);

  categorias: Categoria[] = [];
  nuevaCategoriaNombre = ''; 

  ngOnInit() {
    this.cargarCategorias();
  }

  cargarCategorias() {
    this.categoriaService.getCategorias().subscribe({
      next: (data) => this.categorias = data,
      error: (e) => console.error('Error al cargar categorías:', e)
    });
  }

  crear() {
    if (!this.nuevaCategoriaNombre.trim()) return;
    
    // Creamos el objeto con ID 0 o null (el backend lo asignará)
    const nuevaCat: Categoria = { idCategoria: 0, nombre: this.nuevaCategoriaNombre };
    
    this.adminService.createCategoria(nuevaCat).subscribe({
      next: () => {
        this.nuevaCategoriaNombre = ''; // Limpiar input
        this.cargarCategorias(); // Recargar lista
        alert('Categoría creada con éxito');
      },
      error: (e) => alert('Error al crear la categoría')
    });
  }
}