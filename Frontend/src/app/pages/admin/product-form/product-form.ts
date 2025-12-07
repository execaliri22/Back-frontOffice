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
  styleUrls: ['./product-form.css']
})
export class ProductFormComponent implements OnInit {

  private fb = inject(FormBuilder);
  private adminService = inject(AdminService); // Esto ya debería funcionar
  private categoriaService = inject(CategoriaService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  form: FormGroup;
  categorias: Categoria[] = [];
  esEdicion = false;
  idProducto: number | null = null;

  constructor() {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      sku: ['', Validators.required],
      precio: [0, Validators.required],
      stock: [0, Validators.required],
      urlImagen: [''],
      descripcion: [''],
      ean: [''],
      idCategoria: [null, Validators.required]
    });
  }

  ngOnInit(): void {
    this.categoriaService.getCategorias().subscribe(cats => this.categorias = cats);

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
    // Solución al error de tipos: Tipamos explícitamente 'productos' como Producto[]
    this.adminService.getProductos().subscribe((productos: Producto[]) => {
      const prod = productos.find((p: Producto) => p.idProducto === this.idProducto);
      if (prod) {
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
    });
  }

  guardar() {
    if (this.form.invalid) return;
    const data = this.form.value;

    if (this.esEdicion && this.idProducto) {
      this.adminService.updateProducto(this.idProducto, data).subscribe({
        next: () => this.router.navigate(['/admin/productos']),
        error: (e: any) => alert('Error al actualizar')
      });
    } else {
      this.adminService.createProducto(data).subscribe({
        next: () => this.router.navigate(['/admin/productos']),
        error: (e: any) => alert('Error al crear')
      });
    }
  }
}