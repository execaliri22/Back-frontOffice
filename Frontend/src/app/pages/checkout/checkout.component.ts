import { Component } from '@angular/core';
import { PaymentService } from '../../core/services/payment.service'; // Ajusta la ruta a tu servicio

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})
export class CheckoutComponent {
  procesando = false;
  error: string | null = null;

  constructor(private paymentService: PaymentService) {}

  confirmarPago() {
    this.procesando = true;
    this.error = null;

    this.paymentService.crearPreferencia().subscribe({
      next: (res: any) => {
        if (res.url) {
          // AQUÍ OCURRE LA MAGIA: Redirigimos al usuario a Mercado Pago
          window.location.href = res.url; 
        } else {
          this.error = 'No se recibió el link de pago.';
          this.procesando = false;
        }
      },
      error: (err) => {
        console.error(err);
        this.error = 'Hubo un error al conectar con el servidor de pagos.';
        this.procesando = false;
      }
    });
  }
}