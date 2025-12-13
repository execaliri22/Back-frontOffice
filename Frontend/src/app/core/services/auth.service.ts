import { Injectable, computed, signal, OnDestroy, EventEmitter, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, tap, Subject, catchError, throwError } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/models';

interface JwtPayload {
  sub: string;
  nombre?: string;
  fotoPerfilUrl?: string;
  role?: string;
  authorities?: string[];
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
  
  // --- LÓGICA DE ADMIN ---
  public isAdmin = computed<boolean>(() => {
    const user = this.currentUser();
    if (!user) return false;
    if (user.role === 'ROLE_ADMIN') return true;
    if (user.authorities && Array.isArray(user.authorities)) {
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

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, request).pipe(
      tap(response => this.saveToken(response.token)),
      catchError(err => this.handleError(err, 'REGISTRO'))
    );
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => this.saveToken(response.token)),
      catchError(err => this.handleError(err, 'LOGIN CLIENTE'))
    );
  }

  loginAdmin(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.adminApiUrl}/login`, request).pipe(
      tap(response => this.saveToken(response.token)),
      catchError(err => this.handleError(err, 'LOGIN ADMIN'))
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

  // --- MÉTODOS DE PERFIL ---

  private refreshUserDataFromToken(newToken: string): void {
      this.saveToken(newToken);
  }

  actualizarNombre(nuevoNombre: string): Observable<AuthResponse> {
    return this.http.put<AuthResponse>(`${this.perfilApiUrl}/nombre`, { nombre: nuevoNombre }).pipe(
       tap(response => this.refreshUserDataFromToken(response.token)),
       catchError(err => this.handleError(err, 'ACTUALIZAR NOMBRE'))
    );
  }

  cambiarContrasena(actual: string, nueva: string): Observable<string> {
     return this.http.put(`${this.perfilApiUrl}/contrasena`, { actual, nueva }, { responseType: 'text' }).pipe(
        catchError(err => this.handleError(err, 'CAMBIAR CONTRASEÑA'))
     );
  }

  subirFotoPerfil(archivo: File): Observable<AuthResponse> {
     const formData = new FormData();
     formData.append('file', archivo, archivo.name);
     return this.http.post<AuthResponse>(`${this.perfilApiUrl}/foto`, formData).pipe(
        tap(response => this.refreshUserDataFromToken(response.token)),
        catchError(err => this.handleError(err, 'SUBIR FOTO'))
     );
  }

  eliminarFotoPerfil(): Observable<AuthResponse> {
     return this.http.delete<AuthResponse>(`${this.perfilApiUrl}/foto`).pipe(
        tap(response => this.refreshUserDataFromToken(response.token)),
        catchError(err => this.handleError(err, 'ELIMINAR FOTO'))
     );
  }

  // --- MANEJO DE ERRORES (MODIFICADO PARA DEBUG) ---

  private handleError(error: HttpErrorResponse, context: string): Observable<never> {
    // 🔍 DEBUG: Esto imprimirá el error real en la consola del navegador
    console.group(`🚨 ERROR EN AUTH-SERVICE: ${context}`);
    console.log(`URL: ${error.url}`);
    console.log(`Status: ${error.status} ${error.statusText}`);
    console.log('Respuesta del Backend (Body):', error.error);
    console.groupEnd();

    let userMessage = 'Ocurrió un error inesperado.';

    if (error.status === 0) {
        userMessage = 'Error de conexión. Verifica si el backend está encendido.';
    
    } else if (error.status === 401) {
      // 401: Unauthorized -> Credenciales mal o usuario no encontrado
      userMessage = 'Credenciales incorrectas. Revisa tu correo y contraseña.';
    
    } else if (error.status === 403) {
      // 403: Forbidden -> Spring Security bloqueó la petición
      userMessage = 'Acceso denegado (403). Posible error de configuración CORS o SecurityFilterChain.';
    
    } else if (error.status === 404) {
      userMessage = `No se encontró la dirección: ${error.url}`;
    
    } else if (error.status === 400) {
      // Errores de validación (Bad Request)
        if (typeof error.error === 'string') userMessage = error.error;
        else if (error.error?.message) userMessage = error.error.message;
        else userMessage = 'Datos inválidos. Revisa el formulario.';
    
    } else if (error.status >= 500) {
      userMessage = 'Error interno del servidor. Revisa los logs de Java.';
    }

    // Devolvemos un error con el mensaje procesado para que el componente lo muestre
    return throwError(() => new Error(userMessage));
  }

  private handleStorageChange(event: StorageEvent): void {
    if (event.key === this.TOKEN_KEY) {
      this.updateLoginStatus();
    }
  }
}