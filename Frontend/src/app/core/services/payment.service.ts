import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  // Ajusta la URL si tu backend no está en localhost:8080
  private apiUrl = 'http://localhost:8080/api/pagos';

  constructor(private http: HttpClient) { }

  crearPreferencia(): Observable<any> {
    // Hacemos POST. Si necesitas enviar el carrito real,
    // pasarías el objeto 'carrito' como segundo argumento en lugar de {}.
    return this.http.post(`${this.apiUrl}/crear_preferencia`, {});
  }
}