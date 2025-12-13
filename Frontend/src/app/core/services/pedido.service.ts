import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http'; // <--- IMPORTANTE: HttpParams
import { Observable } from 'rxjs';
import { Pedido } from '../models/models'; // Asegúrate que la ruta sea correcta

@Injectable({
  providedIn: 'root'
})
export class PedidoService {
  
  private http = inject(HttpClient);
  
  // URL base: http://localhost:8080/api/pedidos
  private apiUrl = 'http://localhost:8080/api/pedidos';

  constructor() { }

  // ==========================================
  // 🛒 ZONA CLIENTE (Lo que ya tenías)
  // ==========================================

  // 1. CHECKOUT
  crearPedido(): Observable<Pedido> {
    return this.http.post<Pedido>(this.apiUrl, {});
  }

  // 2. HISTORIAL PROPIO
  getMisPedidos(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(`${this.apiUrl}/mis-pedidos`);
  }

  // ==========================================
  // 👮 ZONA ADMIN (LO NUEVO PARA EL DASHBOARD)
  // ==========================================

  // 3. OBTENER TODOS LOS PEDIDOS (Para la tabla de gestión)
  obtenerTodosLosPedidos(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(`${this.apiUrl}/admin/todos`);
  }

  // 4. CAMBIAR ESTADO (Para el desplegable)
  // Envía el nuevo estado como parámetro en la URL (?nuevoEstado=...)
  actualizarEstado(idPedido: number, nuevoEstado: string): Observable<Pedido> {
    const params = new HttpParams().set('nuevoEstado', nuevoEstado);
    
    return this.http.put<Pedido>(
      `${this.apiUrl}/admin/${idPedido}/estado`, 
      null, // El cuerpo va vacío porque enviamos el dato por params
      { params }
    );
  }
}