import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { catchError, Observable, of } from 'rxjs';
import { Categoria, Producto } from '../../core/models/models';
import { CategoriaService } from '../../core/services/categoria.service';
import { ProductoService } from '../../core/services/producto.service';
import { CommonModule } from '@angular/common';
import { ProductListComponent } from '../../components/product-list/product-list.component';

@Component({
  selector: 'app-tienda',
  standalone: true,
  imports: [
    CommonModule,
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

  public categorias$: Observable<Categoria[]> | undefined;
  public errorCategorias: string | null = null;

  categoriaSeleccionadaId = signal<number | null>(null);
  public productos$: Observable<Producto[]> | undefined;

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
    this.cargarProductos();
  }

  cargarProductos(): void {
      const idCat = this.categoriaSeleccionadaId();
      console.log(`Cargando productos para categoría ID: ${idCat}`);

      const productosObservable = idCat === null
          ? this.productoService.getProductos()
          : this.productoService.getProductosPorCategoria(idCat);

      this.productos$ = productosObservable.pipe(
          catchError(err => {
              console.error(`Error al cargar productos para categoría ${idCat}:`, err);
              return of([]);
          })
      );
  }
}
