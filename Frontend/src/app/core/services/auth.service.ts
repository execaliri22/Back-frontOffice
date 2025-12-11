import { Injectable, computed, signal, OnDestroy, EventEmitter, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, tap, Subject, catchError, throwError } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/models';

// Interface para el payload del JWT
interface JwtPayload {
  sub: string;
  nombre?: string;
  fotoPerfilUrl?: string;
  role?: string;           // Caso 1: Backend envía "role": "ROLE_ADMIN"
  authorities?: string[];  // Caso 2: Backend envía "authorities": ["ROLE_ADMIN"]
  exp?: number;
  iat?: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService implements OnDestroy {
  
  // --- URLs ---
  private apiUrl = 'http://localhost:8080/auth';               // Clientes
  private adminApiUrl = 'http://localhost:8080/api/backoffice/auth'; // Admins
  private perfilApiUrl = 'http://localhost:8080/api/perfil';    // Perfil
  
  private readonly TOKEN_KEY = 'authToken';
  public logoutEvent = new EventEmitter<void>();

  private http = inject(HttpClient);

  // --- SIGNALS DE ESTADO ---
  private loggedInStatus = signal<boolean>(false);
  public isLoggedIn$ = this.loggedInStatus.asReadonly();

  private currentUserToken = signal<string | null>(null);
  
  // Usuario decodificado (Computado: cambia si cambia el token)
  public currentUser = computed<JwtPayload | null>(() => {
    const token = this.currentUserToken();
    if (token) {
      return this.decodeToken(token);
    }
    return null;
  });

  // Helpers computados (Signals)
  public currentUserName = computed<string | null>(() => this.currentUser()?.nombre ?? this.currentUser()?.sub ?? null);
  public currentUserEmail = computed<string | null>(() => this.currentUser()?.sub ?? null);
  public currentUserFotoUrl = computed<string | null>(() => this.currentUser()?.fotoPerfilUrl ?? null);
  
  // --- LÓGICA DE ADMIN (MEJORADA) ---
  public isAdmin = computed<boolean>(() => {
    const user = this.currentUser();
    if (!user) return false;

    // 1. Verificar si viene en el campo 'role' (String simple)
    if (user.role === 'ROLE_ADMIN') return true;

    // 2. Verificar si viene en 'authorities' (Array de strings - Típico de Spring Security)
    if (user.authorities && Array.isArray(user.authorities)) {
      // Buscamos si el array contiene el rol de admin
      return user.authorities.includes('ROLE_ADMIN');
    }

    return false; 
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

  // 3. Login Admin (BackOffice)
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
    this.updateLoginStatus(); // Actualiza los signals
  }

  private clearToken(): void {
    if (typeof localStorage === 'undefined') return;
    localStorage.removeItem(this.TOKEN_KEY);
    this.updateLoginStatus(); // Actualiza los signals
  }

  // Actualiza todos los signals leyendo el localStorage
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

  // Helper simple para usar en condicionales normales (no reactivos)
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

  // --- MÉTODOS DE PERFIL ---

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