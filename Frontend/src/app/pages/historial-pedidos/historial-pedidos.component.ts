import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe, CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { PedidoService } from '../../core/services/pedido.service';
import { Pedido } from '../../core/models/models';

@Component({
  selector: 'app-historial-pedidos',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe, CurrencyPipe],
  templateUrl: './historial-pedidos.component.html',
  styleUrls: ['./historial-pedidos.component.css']
})
export class HistorialPedidosComponent implements OnInit {
  
  private pedidoService = inject(PedidoService);
  pedidos: Pedido[] = [];
  loading = true;

  ngOnInit() {
    this.pedidoService.getMisPedidos().subscribe({
      next: (data) => {
        this.pedidos = data;
        this.loading = false;
      },
      error: (e) => {
        console.error(e);
        this.loading = false;
      }
    });
  }
}