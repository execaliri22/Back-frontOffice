import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AdminService } from '../../../core/services/admin.service';
import { CategoriaService } from '../../../core/services/categoria.service';
import { Categoria, Producto } from '../../../core/models/models';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './product-form.html',
  styleUrls: ['./product-form.css'] // Nota: en Angular 17+ suele ser styleUrl (singular) o styleUrls (plural)
})
export class ProductFormComponent implements OnInit {

  private fb = inject(FormBuilder);
  private adminService = inject(AdminService);
  private categoriaService = inject(CategoriaService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  form: FormGroup;
  categorias: Categoria[] = [];
  esEdicion = false;
  idProducto: number | null = null;

  constructor() {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      sku: ['', Validators.required],
      // Agregamos validación para evitar números negativos
      precio: [0, [Validators.required, Validators.min(0.01)]], 
      stock: [0, [Validators.required, Validators.min(0)]],
      urlImagen: [''],
      descripcion: [''],
      ean: [''],
      idCategoria: [null, Validators.required]
    });
  }

  ngOnInit(): void {
    // Cargamos categorías
    this.categoriaService.getCategorias().subscribe(cats => this.categorias = cats);

    // Detectamos si es edición
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.esEdicion = true;
        this.idProducto = Number(id);
        this.cargarDatos();
      }
    });
  }

  cargarDatos() {
    if (!this.idProducto) return;

    // OPTIMIZACIÓN: Si tu backend lo permite, usa un método getById.
    // Si no tienes ese endpoint, usa tu método anterior de getProductos().find(...)
    
    // Opción A (Recomendada - Requiere endpoint en backend):
    /*
    this.adminService.getProductoById(this.idProducto).subscribe((prod: Producto) => {
       this.llenarFormulario(prod);
    });
    */

    // Opción B (Tu método actual - Funciona pero descarga todo):
    this.adminService.getProductos().subscribe((productos: Producto[]) => {
      const prod = productos.find((p: Producto) => p.idProducto === this.idProducto);
      if (prod) {
        this.llenarFormulario(prod);
      }
    });
  }

  // Método auxiliar para limpiar el código
  private llenarFormulario(prod: Producto) {
    this.form.patchValue({
      nombre: prod.nombre,
      sku: prod.sku,
      precio: prod.precio,
      stock: prod.stock,
      urlImagen: prod.urlImagen,
      descripcion: prod.descripcion,
      ean: prod.ean,
      idCategoria: prod.categoria?.idCategoria
    });
  }

  guardar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched(); // Marca los errores en rojo si el usuario intenta guardar
      return;
    }
    
    const data = this.form.value;

    if (this.esEdicion && this.idProducto) {
      this.adminService.updateProducto(this.idProducto, data).subscribe({
        next: () => this.router.navigate(['/admin/productos']),
        error: (e) => {
          console.error(e);
          alert('Error al actualizar: ' + e.message);
        }
      });
    } else {
      this.adminService.createProducto(data).subscribe({
        next: () => this.router.navigate(['/admin/productos']),
        error: (e) => {
          console.error(e);
          alert('Error al crear: ' + e.message);
        }
      });
    }
  }
}