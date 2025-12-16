import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
// Si usas el select con [(ngModel)], debes importar FormsModule:
// import { FormsModule } from '@angular/forms'; 
import { AdminService } from '../../../core/services/admin.service'; // Ajusta ruta
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-order-list',
  standalone: true,
  // Deja solo CommonModule si no usas [ngModel] bidireccional
  imports: [CommonModule, RouterLink], 
  templateUrl: './order-list.html',
  styleUrls: ['./order-list.css']
})
export class OrderListComponent implements OnInit {

  private adminService = inject(AdminService);
  
  pedidos: any[] = [];
  
  // ESTADOS ACTUALIZADOS según tu ENUM de Java
  estadosPosibles = ['PROCESANDO', 'PAGADO', 'RECHAZADO', 'EN_CAMINO', 'ENTREGADO', 'INCIDENCIA'];

  ngOnInit() {
    this.cargarPedidos();
  }
  cargarPedidos() {
    this.adminService.getPedidos().subscribe({
      next: (data) => {
        // Asigna la lista de pedidos a la variable local 'pedidos'
        this.pedidos = data;
        console.log('Pedidos cargados con éxito:', this.pedidos);
      },
      error: (err) => {
        // Muestra el error, especialmente el 403 que te estaba afectando
        console.error('ERROR AL CARGAR PEDIDOS:', err); 
        alert('Error al cargar pedidos. Verifica tu sesión de administrador.');
      }
    });
  }

  cambiarEstado(pedido: any, event: any) {
    // Si usas (change)="cambiarEstado(p, $event)" en el HTML:
    const nuevoEstado = event.target.value; 

    // Si usas (ngModelChange)="cambiarEstado(p, $event)" en el HTML:
    // const nuevoEstado = event; 
    
    if (!confirm(`¿Cambiar estado del Pedido #${pedido.idPedido} a ${nuevoEstado}?`)) {
        // En el caso de ngModelChange, esta línea no es necesaria.
        // event.target.value = pedido.estado; 
        return;
    }

    this.adminService.actualizarEstadoPedido(pedido.idPedido, nuevoEstado).subscribe({
      next: () => {
        // Actualizamos visualmente después del éxito
        pedido.estado = nuevoEstado; 
        alert('Estado actualizado correctamente.');
      },
      error: (err) => {
        console.error(err);
        alert('Error al actualizar en el servidor. Verifica rol y URL.');
      }
    });
  }

  // Método auxiliar para colores CSS (ajustado a los nuevos estados)
  getClassEstado(estado: string): string {
    switch (estado) {
      case 'PROCESANDO': return 'bg-warning text-dark'; 
      case 'PAGADO': return 'bg-info text-dark';
      case 'RECHAZADO': return 'bg-danger';
      case 'EN_CAMINO': return 'bg-primary'; 
      case 'ENTREGADO': return 'bg-success';
      case 'INCIDENCIA': return 'bg-secondary';
      default: return 'bg-secondary';
    }
  }
}