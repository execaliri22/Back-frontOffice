import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-payment-result',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './payment-result.html', // Asegúrate de tener tu HTML
  styleUrls: ['./payment-result.css']
})
export class PaymentResultComponent implements OnInit {

  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);
  private router = inject(Router);

  // Apuntamos al Backend LOCAL
  private baseUrl = 'http://localhost:8080/api';

  public estado: string = '';
  public paymentId: string | null = null;
  public externalReference: string | null = null; // ID del Pedido
  
  // Variables para la vista
  public titulo: string = 'Procesando...';
  public mensaje: string = 'Estamos confirmando tu pago con el servidor.';
  public icono: string = '⏳';
  public cargando: boolean = true;

  ngOnInit(): void {
    // Leemos los parámetros de Mercado Pago
    this.route.queryParams.subscribe(params => {
      const status = params['collection_status']; // approved, rejected...
      this.paymentId = params['payment_id'];
      this.externalReference = params['external_reference'];

      this.estado = status;

      if (this.estado === 'approved' && this.externalReference) {
        this.confirmarPagoBackend();
      } else {
        this.configurarVistaManual(this.estado);
      }
    });
  }

  confirmarPagoBackend() {
    const url = `${this.baseUrl}/pedidos/${this.externalReference}/confirmar-pago`;

    this.http.put(url, { paymentId: this.paymentId }).subscribe({
      next: () => {
        this.titulo = '¡Pago Aprobado!';
        this.mensaje = `Pedido #${this.externalReference} confirmado.`;
        this.icono = '🎉';
        this.cargando = false;
        
        // Redirigir a "Mis Pedidos" después de 3 segundos
        setTimeout(() => {
           this.router.navigate(['/perfil']); 
        }, 3000);
      },
      error: (err) => {
        console.error(err);
        this.titulo = 'Error de confirmación';
        this.mensaje = 'El pago entró, pero hubo un error al actualizar el pedido.';
        this.icono = '⚠️';
        this.cargando = false;
      }
    });
  }

  private configurarVistaManual(estado: string) {
      this.cargando = false;
      if (estado === 'failure') {
          this.titulo = 'Pago Rechazado';
          this.icono = '❌';
      } else if (estado === 'pending') {
          this.titulo = 'Pago Pendiente';
          this.icono = '⏱️';
      } else {
          this.titulo = 'Estado Desconocido';
          this.icono = '❓';
      }
  }
}