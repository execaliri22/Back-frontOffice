import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Producto, Categoria } from '../models/models'; 

@Injectable({
  providedIn: 'root'
})
export class AdminService { // <--- Asegúrate que diga export class
  
  constructor() { }

  // --- PRODUCTOS (Siguen usando baseUrl) ---
  getProductos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.baseUrl}/productos`);
  }

  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/backoffice';

  createProducto(producto: any): Observable<Producto> {
    return this.http.post<Producto>(`${this.baseUrl}/productos`, producto);
  }

  updateProducto(id: number, producto: any): Observable<Producto> {
    return this.http.put<Producto>(`${this.baseUrl}/productos/${id}`, producto);
  }

  deleteProducto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/productos/${id}`);
  }
  
  // URL específica para categorías (porque el controlador está en /api/categorias)
  private categoriasUrl = 'http://localhost:8080/api/categorias'; 


  // ... createProducto, updateProducto, etc ...

  // --- CATEGORÍAS (Usan la nueva variable) ---
  
  createCategoria(categoria: Categoria): Observable<Categoria> {
    // CAMBIO AQUÍ: Usamos categoriasUrl en vez de baseUrl
    return this.http.post<Categoria>(this.categoriasUrl, categoria);
  }
  
  deleteCategoria(id: number): Observable<void> {
    return this.http.delete<void>(`${this.categoriasUrl}/${id}`);
  }
}