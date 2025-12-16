import { Injectable, inject, OnDestroy, effect } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError, tap, catchError, Subject, map } from 'rxjs';
import { Carrito, ItemCarrito } from '../models/models';
import { AuthService } from './auth.service';

interface AddItemRequest {
  idProducto: number;
  cantidad: number;
}
interface UpdateQuantityRequest {
   cantidad: number;
}

@Injectable({ providedIn: 'root' })
export class CarritoService implements OnDestroy {
  private apiUrl = '/carrito';
  private http = inject(HttpClient);
  private authService = inject(AuthService);

  private carritoSubject = new BehaviorSubject<Carrito | null>(null);
  
  // 1. Observable del Carrito (Objeto completo)
  public carrito$ = this.carritoSubject.asObservable();

  // 2. Observable del Total (FALTABA ESTO)
public total$ = this.carrito$.pipe(
    map(carrito => {
      if (!carrito || !carrito.items) return 0;
      // Calcula el total sumando (precio * cantidad) de cada item
      return carrito.items.reduce((acc, item) => acc + (item.producto.precio * item.cantidad), 0);
    })
  );

  private destroy$ = new Subject<void>();

  constructor() {
    // Reaccionar al estado de login
    effect(() => {
      const loggedIn = this.authService.isLoggedIn();
      console.log('Login status changed in CartService effect:', loggedIn);
      if (loggedIn) {
        this.cargarCarritoInicial();
      } else {
        this.limpiarCarritoLocal();
      }
    });

    if (this.authService.isLoggedIn()) {
       this.cargarCarritoInicial();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // --- Métodos Públicos ---

  public cargarCarritoInicial(): void {
    if (!this.authService.isLoggedIn()) {
        console.warn('Intento de cargar carrito sin estar logueado.');
        this.carritoSubject.next(null);
        return;
    }
    this.http.get<Carrito>(this.apiUrl).pipe(
      tap(carrito => console.log('Carrito cargado:', carrito)),
      catchError(err => this.handleError(err, 'cargarCarritoInicial'))
    ).subscribe({
        next: carrito => this.carritoSubject.next(carrito),
        error: () => this.carritoSubject.next(null)
    });
  }

  agregarItem(idProducto: number, cantidad: number): Observable<Carrito> {
    if (!this.authService.isLoggedIn()) {
      return throwError(() => new Error('Usuario no autenticado. Inicia sesión para agregar productos.'));
    }
    const request: AddItemRequest = { idProducto, cantidad };
    return this.http.post<Carrito>(this.apiUrl, request).pipe(
      tap(carritoActualizado => {
        console.log('Ítem agregado, carrito actualizado:', carritoActualizado);
        this.carritoSubject.next(carritoActualizado);
      }),
      catchError(err => this.handleError(err, 'agregarItem'))
    );
  }

  actualizarCantidad(idItemCarrito: number, nuevaCantidad: number): Observable<Carrito | null> {
     if (!this.authService.isLoggedIn()) {
      return throwError(() => new Error('Usuario no autenticado.'));
    }
    const request: UpdateQuantityRequest = { cantidad: nuevaCantidad };
    return this.http.put<Carrito>(`${this.apiUrl}/items/${idItemCarrito}`, request, { observe: 'response', responseType: 'json' }).pipe(
      tap(response => {
        if (response.status === 200 && response.body) {
           console.log('Cantidad actualizada, carrito:', response.body);
           this.carritoSubject.next(response.body);
        }
        else if (response.status === 204) {
           console.log('Ítem eliminado por cantidad <= 0, recargando carrito...');
           this.cargarCarritoInicial();
        }
      }),
      map(response => response.body),
      catchError(err => this.handleError(err, 'actualizarCantidad'))
    );
  }

  eliminarItem(idItemCarrito: number): Observable<void> {
     if (!this.authService.isLoggedIn()) {
      return throwError(() => new Error('Usuario no autenticado.'));
    }
    return this.http.delete<void>(`${this.apiUrl}/items/${idItemCarrito}`).pipe(
      tap(() => {
        console.log(`Ítem ${idItemCarrito} eliminado, recargando carrito...`);
        this.cargarCarritoInicial();
      }),
      catchError(err => this.handleError(err, 'eliminarItem'))
    );
  }

  private limpiarCarritoLocal(): void {
    this.carritoSubject.next(null);
    console.log('Carrito local limpiado.');
  }

  private handleError(error: HttpErrorResponse, context: string = 'desconocido'): Observable<never> {
    let errorMessage = `Error en ${context}: `;
    if (error.error instanceof ErrorEvent) {
      errorMessage += `Error: ${error.error.message}`;
    } else {
      errorMessage += `Código ${error.status}, Body: ${error.error?.message || error.message || JSON.stringify(error.error)}`;
    }
    console.error(errorMessage, error);
    if (error.status === 401 || error.status === 403) {
         return throwError(() => new Error('No autorizado. Por favor, inicia sesión.'));
    }
     if (error.status === 404) {
         return throwError(() => new Error('Recurso no encontrado.'));
     }
    return throwError(() => new Error(`Ocurrió un error en la operación (${context}). Intenta de nuevo.`));
  }
}