import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Categoria } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class CategoriaService {

  // ⚠️ IMPORTANTE: Verifica que esta sea la URL correcta de tu backend.
  // Si usas un prefijo especial para admin (ej: /api/backoffice/categorias), cámbialo aquí.
  private apiUrl = 'http://localhost:8080/api/categorias'; 

  private http = inject(HttpClient);

  constructor() { }

  // 1. Obtener todas (Ya lo tenías)
  getCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.apiUrl);
  }

  // 2. Obtener una por ID (Opcional, útil para editar en página separada)
  getCategoriaById(id: number): Observable<Categoria> {
    return this.http.get<Categoria>(`${this.apiUrl}/${id}`);
  }

  // --- MÉTODOS QUE TE FALTABAN ---

  // 3. Crear (POST)
  // Recibe un objeto parcial (sin ID) y devuelve la categoría creada
  createCategoria(categoria: Partial<Categoria>): Observable<Categoria> {
    return this.http.post<Categoria>(this.apiUrl, categoria);
  }

  // 4. Actualizar (PUT)
  // Necesita el ID en la URL y el objeto con los cambios en el body
  updateCategoria(id: number, categoria: Partial<Categoria>): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.apiUrl}/${id}`, categoria);
  }

  // 5. Eliminar (DELETE)
  // Solo necesita el ID en la URL
  deleteCategoria(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}