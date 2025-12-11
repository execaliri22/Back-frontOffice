import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CategoriaService } from '../../../core/services/categoria.service'; // O AdminService si lo tienes ahí
import { Categoria } from '../../../core/models/models';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './category-list.html',
  styleUrls: ['./category-list.css']
})
export class CategoryListComponent implements OnInit {

  private fb = inject(FormBuilder);
  private categoriaService = inject(CategoriaService);

  categorias: Categoria[] = [];
  form: FormGroup;
  
  // Variable para controlar si estamos editando
  categoriaEnEdicion: Categoria | null = null;

  constructor() {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      descripcion: [''] // Agrego descripción por si la usas
    });
  }

  ngOnInit(): void {
    this.cargarCategorias();
  }

  cargarCategorias() {
    this.categoriaService.getCategorias().subscribe(data => {
      this.categorias = data;
    });
  }

  // Lógica unificada: Crear o Actualizar
  guardar() {
    if (this.form.invalid) return;

    const data = this.form.value;

    if (this.categoriaEnEdicion) {
      // --- MODO EDICIÓN ---
      const id = this.categoriaEnEdicion.idCategoria;
      this.categoriaService.updateCategoria(id, data).subscribe(() => {
        this.cargarCategorias();
        this.cancelarEdicion(); // Limpia el form
      });
    } else {
      // --- MODO CREACIÓN ---
      this.categoriaService.createCategoria(data).subscribe(() => {
        this.cargarCategorias();
        this.form.reset();
      });
    }
  }

  // Carga los datos en el input de arriba
  editar(cat: Categoria) {
    this.categoriaEnEdicion = cat;
    this.form.patchValue({
      nombre: cat.nombre,
      descripcion: cat.descripcion
    });
  }

  // Borrar
  eliminar(id: number) {
    if (confirm('¿Seguro que deseas eliminar esta categoría?')) {
      this.categoriaService.deleteCategoria(id).subscribe(() => {
        this.cargarCategorias();
        // Si borramos la que estábamos editando, limpiamos el form
        if (this.categoriaEnEdicion?.idCategoria === id) {
          this.cancelarEdicion();
        }
      });
    }
  }

  // Botón "Cancelar" para salir del modo edición
  cancelarEdicion() {
    this.categoriaEnEdicion = null;
    this.form.reset();
  }
}
