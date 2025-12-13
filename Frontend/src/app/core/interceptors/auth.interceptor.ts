import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService); 
  const authToken = authService.getToken();

  // 1. LISTA BLANCA (WHITELIST):
  // Si la petición va dirigida al Login o Registro, NO adjuntamos el token.
  // Esto evita enviar "basura" (tokens viejos) que causan el error 403 en el servidor.
  if (req.url.includes('/auth/login') || req.url.includes('/auth/register')) {
    return next(req);
  }

  // 2. Para el resto de peticiones, si hay token, lo adjuntamos.
  if (authToken) {
    const authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${authToken}`)
    });
    return next(authReq);
  }
  
  return next(req);
};