import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe, CurrencyPipe } from '@angular/common'; // Pipes para fecha y dinero
import { RouterLink } from '@angular/router';
import { AdminService } from '../../../core/services/admin.service';
import { Pedido } from '../../../core/models/models';
import { FormsModule } from '@angular/forms'; // Para el select de estado

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, DatePipe, CurrencyPipe],
  templateUrl: './order-list.html', // Ajusta si se generó como .component.html
  styleUrls: ['./order-list.css']
})
export class OrderListComponent implements OnInit {
  
  private adminService = inject(AdminService);
  
  pedidos: Pedido[] = [];
  estadosPosibles = ['PROCESANDO', 'ENVIADO', 'ENTREGADO', 'CANCELADO']; // Ajusta a tus ENUM de Java

  ngOnInit() {
    this.cargarPedidos();
  }

  cargarPedidos() {
    this.adminService.getPedidos().subscribe({
      next: (data) => this.pedidos = data,
      error: (e) => console.error('Error cargando pedidos', e)
    });
  }

  cambiarEstado(pedido: Pedido, nuevoEstado: string) {
    if(!confirm(`¿Cambiar estado a ${nuevoEstado}?`)) return;

    this.adminService.updateEstadoPedido(pedido.idPedido, nuevoEstado).subscribe({
      next: (pedidoActualizado) => {
        alert('Estado actualizado');
        pedido.estado = pedidoActualizado.estado; // Actualizamos la vista localmente
      },
      error: (e) => alert('Error al actualizar estado')
    });
  }
}