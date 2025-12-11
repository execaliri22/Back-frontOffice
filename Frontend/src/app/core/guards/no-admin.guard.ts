import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const noAdminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // 1. Verificamos si es admin (necesitas tener este método en tu servicio)
  if (authService.isAdmin()) {
    // 2. Si es admin, lo pateamos al dashboard
    router.navigate(['/admin']);
    return false; // Bloquea la navegación a la tienda
  }

  // 3. Si no es admin (es cliente o no está logueado), pasa.
  return true;
};