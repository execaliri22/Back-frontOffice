import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

// --- IMPORTACIONES CLIENTE ---
import { TiendaComponent } from './pages/tienda/tienda.component';
import { AuthComponent } from './pages/auth/auth.component'; // O LoginComponent si le cambiaste el nombre
import { CarritoComponent } from './pages/carrito/carrito.component';
import { CheckoutComponent } from './pages/checkout/checkout.component';
import { HistorialPedidosComponent } from './pages/historial-pedidos/historial-pedidos.component';
import { FavoritosComponent } from './pages/favoritos/favoritos.component';
import { PerfilComponent } from './pages/perfil/perfil.component';

// --- IMPORTACIONES ADMIN (NUEVAS) ---
import { ProductListComponent } from './pages/admin/product-list/product-list';
import { ProductFormComponent } from './pages/admin/product-form/product-form';
import { AdminDashboardComponent } from './pages/admin/admin-dashboard/admin-dashboard';
import { CategoryListComponent } from './pages/admin/category-list/category-list';
import { OrderListComponent } from './pages/admin/order-list/order-list';

export const routes: Routes = [
  // ==========================================
  // ZONA PÚBLICA
  // ==========================================
  { path: '', redirectTo: '/tienda', pathMatch: 'full' },
  { path: 'tienda', component: TiendaComponent },
  { path: 'auth', component: AuthComponent }, // Login de usuario/admin
  { path: 'admin', component: AdminDashboardComponent, canActivate: [authGuard] },
  
// ==========================================
  // ZONA CLIENTE (Protegida)
  // ==========================================
  { path: 'carrito', component: CarritoComponent, canActivate: [authGuard] },
  { path: 'checkout', component: CheckoutComponent, canActivate: [authGuard] },
  { path: 'historial', component: HistorialPedidosComponent, canActivate: [authGuard] },
  { path: 'favoritos', component: FavoritosComponent, canActivate: [authGuard] },
  { path: 'perfil', component: PerfilComponent, canActivate: [authGuard] },

  // ==========================================
  // ZONA ADMIN / BACKOFFICE (Protegida)
  // ==========================================
  { 
    path: 'admin/productos', 
    component: ProductListComponent, 
    canActivate: [authGuard] 
  },
  { 
    path: 'admin/productos/nuevo', 
    component: ProductFormComponent, 
    canActivate: [authGuard] 
  },
  { 
    path: 'admin/productos/editar/:id', 
    component: ProductFormComponent, 
    canActivate: [authGuard] 
  },
  { 
    path: 'admin/categorias', 
    component: CategoryListComponent, 
    canActivate: [authGuard] 
  },
  { 
  path: 'admin/pedidos', 
  component: OrderListComponent, 
  canActivate: [authGuard] 
},

  // ==========================================
  // WILDCARD (Error 404)
  // ==========================================
  { path: '**', redirectTo: '/tienda' }
];