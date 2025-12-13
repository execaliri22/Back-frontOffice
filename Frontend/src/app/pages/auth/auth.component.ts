import { Component } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { LoginRequest, RegisterRequest } from '../../core/models/models';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.css']
})
export class AuthComponent {
  public esLogin = true; 
  loginForm: FormGroup;
  registerForm: FormGroup;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
    this.registerForm = this.fb.group({
      nombre: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      direccion: ['', [Validators.required]]
    });
  }

onLogin() {
    // 1. DIAGNÓSTICO DE FORMULARIO
    if (this.loginForm.invalid) {
      console.warn('Login bloqueado: El formulario no es válido', this.loginForm.value);
      this.loginForm.markAllAsTouched(); // Esto hará que los inputs se pongan rojos si faltan datos
      return;
    }
    
    this.error = null;
    const request: LoginRequest = this.loginForm.value;

    console.log('Intentando login con:', request); // Verificamos qué datos salen

    // Lógica de Admin vs Cliente
    if (request.email === 'admin@admin.com') {
       // ... tu lógica de admin igual que antes ...
       this.authService.loginAdmin(request).subscribe({
        next: (resp) => {
          console.log('Login ADMIN ok:', resp);
          this.router.navigate(['/admin']);
        },
        error: (err) => {
          console.error(err);
          this.error = 'Error credenciales Admin';
        }
      });
    } else {
      // CASO CLIENTE
      this.authService.login(request).subscribe({
        next: (response) => {
          // 2. DIAGNÓSTICO DE TOKEN (¡Punto Crítico!)
          console.log('Respuesta del Login:', response); 

          // IMPORTANTE: ¿Tu servicio guarda el token automáticamente?
          // Si tu servicio NO usa .pipe(tap(...)) para guardar el token, 
          // debes guardarlo aquí manualmente, o el Guard te expulsará de /tienda.
          // Ejemplo: localStorage.setItem('token', response.token);

          console.log('Navegando a /tienda...');
          this.router.navigate(['/tienda']);
        },
        error: (err) => {
          console.error('Error de API en Login:', err);
          this.error = 'Credenciales incorrectas o error de servidor.';
        }
      });
    }
  }

onRegister() {
    // 1. Verificamos si es inválido
    if (this.registerForm.invalid) {
      console.warn('El formulario de registro no es válido.', this.registerForm.value);
      
      // ESTO ES CLAVE: Marca todos los campos como "tocados" para que el HTML muestre los errores en rojo
      this.registerForm.markAllAsTouched(); 
      return;
    }

    this.error = null;
    const request: RegisterRequest = this.registerForm.value;

    console.log('Enviando registro...', request); // Para ver qué datos envías

    this.authService.register(request).subscribe({
      next: (res) => {
        console.log('Registro exitoso, respuesta del server:', res);
        // Si el backend devuelve un token aquí, asegúrate de guardarlo antes de redirigir
        // this.authService.setToken(res.token); (si aplica)
        
        this.router.navigate(['/tienda']);
      },
      error: (err) => {
        console.error('Error en el registro:', err);
        this.error = 'Error al registrar. ¿El email ya existe o el servidor falló?';
      }
    });
  }
}