import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Pedido } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class PedidoService {
  
  private http = inject(HttpClient);
  
  // URL base de tus pedidos en el Backend
  private apiUrl = 'http://localhost:8080/api/pedidos';

  constructor() { }

  // 1. CHECKOUT REAL
  // Llama al POST /api/pedidos
  // El backend busca al usuario por el token, toma su carrito y crea el pedido.
  crearPedido(): Observable<Pedido> {
    return this.http.post<Pedido>(this.apiUrl, {});
  }

  // 2. HISTORIAL REAL
  // Llama al GET /api/pedidos/mis-pedidos
  getMisPedidos(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(`${this.apiUrl}/mis-pedidos`);
  }
}