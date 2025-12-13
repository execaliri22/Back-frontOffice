import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-payment-result',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './payment-result.html',
  styleUrls: ['./payment-result.css']
})
export class PaymentResultComponent implements OnInit {

  private route = inject(ActivatedRoute);

  public estado: string = ''; // 'exitoso', 'pendiente', 'fallo'
  public paymentId: string | null = null;
  
  // Variables para la vista
  public titulo: string = '';
  public mensaje: string = '';
  public icono: string = ''; // Usaremos emojis o clases css

  ngOnInit(): void {
    // 1. Obtener el estado definido en las rutas (app.routes.ts)
    this.route.data.subscribe(data => {
      this.estado = data['estado'] || 'desconocido';
      this.configurarVista();
    });

    // 2. Obtener el ID de pago que manda Mercado Pago en la URL (?payment_id=...)
    this.route.queryParams.subscribe(params => {
      this.paymentId = params['payment_id'] || params['collection_id'] || null;
    });
  }

  private configurarVista() {
    switch (this.estado) {
      case 'exitoso':
        this.titulo = '¡Pago Aprobado!';
        this.mensaje = 'Tu compra ha sido procesada correctamente. Te hemos enviado un correo con los detalles.';
        this.icono = '🎉';
        break;
      
      case 'pendiente':
        this.titulo = 'Pago Pendiente';
        this.mensaje = 'Estamos esperando la confirmación del pago. No te preocupes, te avisaremos en cuanto se acredite.';
        this.icono = '⏳';
        break;
      
      case 'fallo':
        this.titulo = 'Pago Rechazado';
        this.mensaje = 'Hubo un problema con tu tarjeta o medio de pago. Por favor, intenta nuevamente.';
        this.icono = '❌';
        break;

      default:
        this.titulo = 'Estado Desconocido';
        this.mensaje = 'No pudimos determinar el estado de tu pago.';
        this.icono = '❓';
    }
  }
}