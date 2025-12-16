import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Producto, Categoria, Pedido } from '../models/models'; // <--- Agregamos Pedido
import { AuthService } from './auth.service';
@Injectable({
  providedIn: 'root'
})
export class AdminService {

  private authService = inject(AuthService);
  // 1. PROPIEDADES AL PRINCIPIO (Mejor práctica)
  private http = inject(HttpClient);
  
  // URL para Productos (Backoffice)
  private baseUrl = 'http://localhost:8080/api/backoffice';
  
  // URL para Categorías (Directa)
  private categoriasUrl = 'http://localhost:8080/api/categorias'; 

  // URL para Pedidos (Directa al controlador de pedidos)
  private pedidosUrl = 'http://localhost:8080/api/admin/pedidos';

  constructor() { }

  // ----------------------------------------------------------------
  // 📦 GESTIÓN DE PRODUCTOS
  // ----------------------------------------------------------------

  getProductos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.baseUrl}/productos`);
  }

  createProducto(producto: any): Observable<Producto> {
    return this.http.post<Producto>(`${this.baseUrl}/productos`, producto);
  }

  updateProducto(id: number, producto: any): Observable<Producto> {
    return this.http.put<Producto>(`${this.baseUrl}/productos/${id}`, producto);
  }

  deleteProducto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/productos/${id}`);
  }

  // ----------------------------------------------------------------
  // 🏷️ GESTIÓN DE CATEGORÍAS
  // ----------------------------------------------------------------
  
  getCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.categoriasUrl);
  }

  createCategoria(categoria: Categoria): Observable<Categoria> {
    return this.http.post<Categoria>(this.categoriasUrl, categoria);
  }
  
  deleteCategoria(id: number): Observable<void> {
    return this.http.delete<void>(`${this.categoriasUrl}/${id}`);
  }

  // ----------------------------------------------------------------
  // 🚚 GESTIÓN DE PEDIDOS (¡NUEVO!)
  // ----------------------------------------------------------------

  getPedidos(): Observable<any[]> {
    // LLAMA A GET /api/admin/pedidos
    return this.http.get<any[]>(this.pedidosUrl, { headers: this.getHeaders() });
  }

  actualizarEstadoPedido(idPedido: number, nuevoEstado: string): Observable<any> {
    // LLAMA A PUT /api/admin/pedidos/{id}/estado
    return this.http.put(`${this.pedidosUrl}/${idPedido}/estado`, {}, {
      headers: this.getHeaders(),
      params: { nuevoEstado: nuevoEstado }
    });
  }

  // Helper para enviar el Token en cada petición (CRUDO pero efectivo)
  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken(); // Asegúrate de tener getToken() en AuthService
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }
}