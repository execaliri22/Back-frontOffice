import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { noAdminGuard } from './core/guards/no-admin.guard'; // <--- 1. IMPORTAR EL GUARD

// --- IMPORTACIONES CLIENTE ---
import { TiendaComponent } from './pages/tienda/tienda.component';
import { AuthComponent } from './pages/auth/auth.component';
import { CarritoComponent } from './pages/carrito/carrito.component';
import { CheckoutComponent } from './pages/checkout/checkout.component';
import { HistorialPedidosComponent } from './pages/historial-pedidos/historial-pedidos.component';
import { FavoritosComponent } from './pages/favoritos/favoritos.component';
import { PerfilComponent } from './pages/perfil/perfil.component';

// --- IMPORTACIONES ADMIN ---
import { ProductListComponent } from './pages/admin/product-list/product-list';
import { ProductFormComponent } from './pages/admin/product-form/product-form';
import { AdminDashboardComponent } from './pages/admin/admin-dashboard/admin-dashboard';
import { CategoryListComponent } from './pages/admin/category-list/category-list';
import { OrderListComponent } from './pages/admin/order-list/order-list';

export const routes: Routes = [
  // ==========================================
  // ZONA PÚBLICA (Pero vetada para Admin)
  // ==========================================
  { path: '', redirectTo: '/tienda', pathMatch: 'full' },
  
  { 
    path: 'tienda', 
    component: TiendaComponent,
    canActivate: [noAdminGuard] // <--- 2. EL ADMIN NO ENTRA AQUÍ
  },
  
  { path: 'auth', component: AuthComponent },

  // ==========================================
  // ZONA CLIENTE (Protegida: Login + No Admin)
  // ==========================================
  { 
    path: 'carrito', 
    component: CarritoComponent, 
    canActivate: [authGuard, noAdminGuard] // <--- 3. Logueado Y NO Admin
  },
  { 
    path: 'checkout', 
    component: CheckoutComponent, 
    canActivate: [authGuard, noAdminGuard] 
  },
  { 
    path: 'historial', 
    component: HistorialPedidosComponent, 
    canActivate: [authGuard, noAdminGuard] 
  },
  { 
    path: 'favoritos', 
    component: FavoritosComponent, 
    canActivate: [authGuard, noAdminGuard] 
  },
  { 
    path: 'perfil', 
    component: PerfilComponent, 
    canActivate: [authGuard, noAdminGuard] 
  },

  // ==========================================
  // ZONA ADMIN / BACKOFFICE (Protegida)
  // ==========================================
  // Nota: Aquí idealmente usarías un 'adminGuard', pero por ahora el authGuard verifica login.
  // Si un usuario normal intenta entrar aquí, deberías crear un guard inverso (adminGuard).
  
  { path: 'admin', component: AdminDashboardComponent, canActivate: [authGuard] },
  
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