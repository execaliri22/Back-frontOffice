import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Producto, Categoria, Pedido } from '../models/models'; // <--- Agregamos Pedido

@Injectable({
  providedIn: 'root'
})
export class AdminService {

  // 1. PROPIEDADES AL PRINCIPIO (Mejor práctica)
  private http = inject(HttpClient);
  
  // URL para Productos (Backoffice)
  private baseUrl = 'http://localhost:8080/api/backoffice';
  
  // URL para Categorías (Directa)
  private categoriasUrl = 'http://localhost:8080/api/categorias'; 

  // URL para Pedidos (Directa al controlador de pedidos)
  private pedidosUrl = 'http://localhost:8080/api/pedidos';

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

  getPedidos(): Observable<Pedido[]> {
    // GET /api/pedidos/admin/todos
    return this.http.get<Pedido[]>(`${this.pedidosUrl}/admin/todos`);
  }

  updateEstadoPedido(id: number, estado: string): Observable<Pedido> {
    // PUT /api/pedidos/admin/{id}/estado?nuevoEstado={estado}
    // Se envía un cuerpo vacío {} porque los datos van en la URL y QueryParams
    return this.http.put<Pedido>(`${this.pedidosUrl}/admin/${id}/estado?nuevoEstado=${estado}`, {});
  }
}