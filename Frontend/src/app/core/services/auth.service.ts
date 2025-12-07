import { Injectable, computed, signal, OnDestroy, EventEmitter, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, tap, Subject, catchError, throwError } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/models';

// Interface para el payload del JWT
interface JwtPayload {
  sub: string;
  nombre?: string;
  fotoPerfilUrl?: string;
  role?: string; // <-- AÑADIDO: Para saber si es ADMIN o USER
  exp?: number;
  iat?: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService implements OnDestroy {
  
  // --- URLs ---
  private apiUrl = 'http://localhost:8080/auth';                // Clientes
  private adminApiUrl = 'http://localhost:8080/api/backoffice/auth'; // Admins (NUEVO)
  private perfilApiUrl = 'http://localhost:8080/api/perfil';    // Perfil
  
  private readonly TOKEN_KEY = 'authToken';
  public logoutEvent = new EventEmitter<void>();

  private http = inject(HttpClient);

  // --- SIGNALS DE ESTADO ---
  private loggedInStatus = signal<boolean>(false);
  public isLoggedIn$ = this.loggedInStatus.asReadonly();

  private currentUserToken = signal<string | null>(null);
  
  // Usuario decodificado
  public currentUser = computed<JwtPayload | null>(() => {
    const token = this.currentUserToken();
    if (token) {
      return this.decodeToken(token);
    }
    return null;
  });

  // Helpers computados
  public currentUserName = computed<string | null>(() => this.currentUser()?.nombre ?? this.currentUser()?.sub ?? null);
  public currentUserEmail = computed<string | null>(() => this.currentUser()?.sub ?? null);
  public currentUserFotoUrl = computed<string | null>(() => this.currentUser()?.fotoPerfilUrl ?? null);
  
  // Helper para saber si es Admin (NUEVO)
  public isAdmin = computed<boolean>(() => {
    const role = this.currentUser()?.role; 
    // Asegúrate de que tu backend mande el claim "role": "ROLE_ADMIN" en el token
    // O si usas "authorities", ajusta la lógica aquí.
    return role === 'ROLE_ADMIN'; 
  });

  private destroy$ = new Subject<void>();

  constructor() {
    if (typeof localStorage !== 'undefined') {
      this.updateLoginStatus();
      window.addEventListener('storage', this.handleStorageChange.bind(this));
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (typeof window !== 'undefined') {
       window.removeEventListener('storage', this.handleStorageChange.bind(this));
    }
  }

  // --- MÉTODOS DE AUTENTICACIÓN ---

  // 1. Registro Cliente
  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, request).pipe(
      tap(response => this.saveToken(response.token)),
      catchError(err => this.handleError(err, 'registro'))
    );
  }

  // 2. Login Cliente (Tienda)
  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => this.saveToken(response.token)),
      catchError(err => this.handleError(err, 'login cliente'))
    );
  }

  // 3. Login Admin (BackOffice) - ¡NUEVO!
  loginAdmin(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.adminApiUrl}/login`, request).pipe(
      tap(response => this.saveToken(response.token)),
      catchError(err => this.handleError(err, 'login admin'))
    );
  }

  logout(): void {
    this.clearToken();
    this.logoutEvent.emit();
    console.log('Usuario deslogueado.');
  }

  // --- MANEJO DE TOKENS ---

  private saveToken(token: string): void {
    if (typeof localStorage === 'undefined') return;
    localStorage.setItem(this.TOKEN_KEY, token);
    this.updateLoginStatus();
  }

  private clearToken(): void {
    if (typeof localStorage === 'undefined') return;
    localStorage.removeItem(this.TOKEN_KEY);
    this.updateLoginStatus();
  }

  private updateLoginStatus(): void {
    if (typeof localStorage === 'undefined') return;
    const token = localStorage.getItem(this.TOKEN_KEY);
    this.currentUserToken.set(token);
    this.loggedInStatus.set(!!token);
  }

  getToken(): string | null {
    if (typeof localStorage === 'undefined') return null;
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return this.loggedInStatus();
  }

  private decodeToken(token: string): JwtPayload | null {
     if (typeof atob === 'undefined') return null;
     try {
       const payloadBase64 = token.split('.')[1];
       if (!payloadBase64) return null;
       const payloadDecoded = atob(payloadBase64);
       return JSON.parse(payloadDecoded) as JwtPayload;
     } catch (error) {
       console.error('Error decodificando token:', error);
       this.clearToken();
       return null;
     }
  }

  // --- MÉTODOS DE PERFIL (Igual que antes) ---

  private refreshUserDataFromToken(newToken: string): void {
      this.saveToken(newToken);
  }

  actualizarNombre(nuevoNombre: string): Observable<AuthResponse> {
    return this.http.put<AuthResponse>(`${this.perfilApiUrl}/nombre`, { nombre: nuevoNombre }).pipe(
       tap(response => this.refreshUserDataFromToken(response.token)),
       catchError(err => this.handleError(err, 'actualizar nombre'))
    );
  }

  cambiarContrasena(actual: string, nueva: string): Observable<string> {
     return this.http.put(`${this.perfilApiUrl}/contrasena`, { actual, nueva }, { responseType: 'text' }).pipe(
        catchError(err => this.handleError(err, 'cambiar contraseña'))
     );
  }

  subirFotoPerfil(archivo: File): Observable<AuthResponse> {
     const formData = new FormData();
     formData.append('file', archivo, archivo.name);
     return this.http.post<AuthResponse>(`${this.perfilApiUrl}/foto`, formData).pipe(
        tap(response => this.refreshUserDataFromToken(response.token)),
        catchError(err => this.handleError(err, 'subir foto'))
     );
  }

  eliminarFotoPerfil(): Observable<AuthResponse> {
     return this.http.delete<AuthResponse>(`${this.perfilApiUrl}/foto`).pipe(
        tap(response => this.refreshUserDataFromToken(response.token)),
        catchError(err => this.handleError(err, 'eliminar foto'))
     );
  }

  // --- MANEJO DE ERRORES ---

  private handleError(error: HttpErrorResponse, context: string): Observable<never> {
    console.error(`Error en ${context}:`, error);
    let userMessage = 'Ocurrió un error inesperado.';

    if (error.status === 0) {
        userMessage = 'Error de conexión con el servidor.';
    } else if (error.status === 401 || error.status === 403) {
      userMessage = 'Credenciales incorrectas o sesión expirada.';
    } else if (error.status === 400) {
        if (typeof error.error === 'string') userMessage = error.error;
        else if (error.error?.message) userMessage = error.error.message;
        else userMessage = 'Datos inválidos.';
    } else if (error.status >= 500) {
      userMessage = 'Error interno del servidor.';
    }

    return throwError(() => new Error(userMessage));
  }

  private handleStorageChange(event: StorageEvent): void {
    if (event.key === this.TOKEN_KEY) {
      this.updateLoginStatus();
    }
  }
}