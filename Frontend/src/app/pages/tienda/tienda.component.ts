import { Component, OnInit, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { catchError, Observable, of } from 'rxjs';
import { Categoria, Producto } from '../../core/models/models';
import { CategoriaService } from '../../core/services/categoria.service';
import { ProductoService } from '../../core/services/producto.service';
import { CommonModule } from '@angular/common';
import { ProductListComponent } from '../../components/product-list/product-list.component';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-tienda',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ProductListComponent
  ],
  templateUrl: './tienda.component.html',
  styleUrls: ['./tienda.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TiendaComponent implements OnInit {
  bannerUrl: string = 'https://i.ibb.co/4RTpzgk5/banner3feel.png';

  private categoriaService = inject(CategoriaService);
  private productoService = inject(ProductoService);

  // Categorías (Se mantiene igual con Observable para el AsyncPipe)
  public categorias$: Observable<Categoria[]> | undefined;
  public errorCategorias: string | null = null;
  categoriaSeleccionadaId = signal<number | null>(null);

  // --- LÓGICA DE FILTRADO ---

  // 1. Signal para el texto del buscador
  terminoBusqueda = signal<string>('');

  // 2. Signal para guardar los productos "crudos" que vienen de la API
  private productosOriginales = signal<Producto[]>([]);

  // 3. Signal COMPUTADO: Aplica DOBLE FILTRO (Estado activo + Búsqueda)
  productosFiltrados = computed(() => {
    const productos = this.productosOriginales();
    const texto = this.terminoBusqueda().toLowerCase().trim();

    // PASO 1: Filtrar solo productos ACTIVOS (CRÍTICO para la tienda)
    const productosActivos = productos.filter(p => p.activo === true);

    if (!texto) {
      return productosActivos; // Si no hay texto, devuelve solo los activos
    }

    // PASO 2: Filtrar por término de búsqueda solo el subconjunto activo
    return productosActivos.filter(p =>
      p.nombre.toLowerCase().includes(texto) ||
      (p.descripcion && p.descripcion.toLowerCase().includes(texto))
    );
  });

  ngOnInit(): void {
    this.cargarCategorias();
    this.cargarProductos();
  }

  cargarCategorias(): void {
    this.errorCategorias = null;
    this.categorias$ = this.categoriaService.getCategorias().pipe(
      catchError(err => {
        this.errorCategorias = err.message || 'Error cargando categorías.';
        console.error('Error al cargar categorías:', err);
        return of([]);
      })
    );
  }

  seleccionarCategoria(idCategoria: number | null): void {
    console.log('Categoría seleccionada:', idCategoria);
    this.categoriaSeleccionadaId.set(idCategoria);

    // Al cambiar categoría, limpiamos el buscador para evitar confusiones (opcional)
    this.terminoBusqueda.set('');

    this.cargarProductos();
  }

  cargarProductos(): void {
    const idCat = this.categoriaSeleccionadaId();
    console.log(`Cargando productos para categoría ID: ${idCat}`);

    const productosObservable = idCat === null
      ? this.productoService.getProductos()
      : this.productoService.getProductosPorCategoria(idCat);

    // Nos suscribimos manualmente para guardar los datos en el Signal
    productosObservable.subscribe({
      next: (data) => {
        this.productosOriginales.set(data);
      },
      error: (err) => {
        console.error(`Error al cargar productos para categoría ${idCat}:`, err);
        this.productosOriginales.set([]);
      }
    });
  }
}