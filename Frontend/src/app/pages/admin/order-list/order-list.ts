import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe, CurrencyPipe } from '@angular/common'; 
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms'; 
import { PedidoService } from 'src/app/core/services/pedido.service';
import { Pedido } from 'src/app/core/models/models';


@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, DatePipe, CurrencyPipe],
  templateUrl: './order-list.html', // Verifica si es .component.html o solo .html
  styleUrls: ['./order-list.css']
})
export class OrderListComponent implements OnInit {
  
  // Inyectamos el servicio que tiene la lógica de Admin
  private pedidoService = inject(PedidoService);
  
  pedidos: Pedido[] = [];
  cargando: boolean = true;

  // IMPORTANTE: Agregué 'PAGADO' que faltaba
  estadosPosibles = ['PROCESANDO', 'PAGADO', 'EN_CAMINO', 'ENTREGADO', 'CANCELADO', 'RECHAZADO'];

  ngOnInit() {
    this.cargarPedidos();
  }

  cargarPedidos() {
    this.cargando = true;
    this.pedidoService.obtenerTodosLosPedidos().subscribe({
      next: (data) => {
        this.pedidos = data;
        // Ordenar por ID descendente (lo más nuevo arriba)
        this.pedidos.sort((a, b) => b.idPedido - a.idPedido);
        this.cargando = false;
      },
      error: (e) => {
        console.error('Error cargando pedidos', e);
        this.cargando = false;
      }
    });
  }

  cambiarEstado(pedido: Pedido, nuevoEstado: string) {
    if(!confirm(`¿Estás seguro de cambiar el pedido #${pedido.idPedido} a ${nuevoEstado}?`)) {
      this.cargarPedidos(); // Revertir visualmente si cancela
      return;
    }

    this.pedidoService.actualizarEstado(pedido.idPedido, nuevoEstado).subscribe({
      next: (pedidoActualizado) => {
        // Actualizamos la vista localmente
        pedido.estado = pedidoActualizado.estado; 
        // Opcional: alert('Estado actualizado correctamente');
      },
      error: (e) => {
        console.error(e);
        alert('Error al actualizar estado en el servidor');
        this.cargarPedidos(); // Revertir en caso de error
      }
    });
  }
}