import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Categoria } from '../models/models'; // Ajusta la ruta si es necesario

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  
  // CAMBIO CLAVE: Usamos la URL completa del Backend
  // Si usas proxy.conf.json, puedes dejarlo como '/api/categorias'
  private apiUrl = 'http://localhost:8080/api/categorias'; 
  
  private http = inject(HttpClient);

  // GET /api/categorias (Público para los filtros)
  getCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.apiUrl).pipe(
       catchError(this.handleError)
    );
  }

  // Manejo de errores
  private handleError(error: any): Observable<never> {
    console.error('Error en CategoriaService:', error);
    return throwError(() => new Error('Error al cargar las categorías.'));
  }
}