import { Component } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { LoginRequest, RegisterRequest } from '../../core/models/models';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule
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
    if (this.loginForm.invalid) return;
    
    this.error = null;
    const request: LoginRequest = this.loginForm.value;

    // 1. CASO ADMIN
    if (request.email === 'admin@admin.com') {
      this.authService.loginAdmin(request).subscribe({
        next: () => {
          console.log('Login Admin Exitoso');
          this.router.navigate(['/admin']);
        },
        error: (err) => {
          console.error(err);
          this.error = 'Error de Admin: Credenciales incorrectas.';
        }
      });

    } else {
      // 2. CASO CLIENTE
      this.authService.login(request).subscribe({
        next: () => {
          console.log('Login Cliente Exitoso');
          this.router.navigate(['/tienda']);
        },
        error: (err) => {
          console.error(err);
          // Intentamos leer el mensaje del backend si existe
          const msg = err.error?.message || 'Email o contraseña incorrectos.';
          this.error = msg;
        }
      });
    }
  }

  onRegister() {
    if (this.registerForm.invalid) return;
    this.error = null;
    const request: RegisterRequest = this.registerForm.value;
    
    // IMPORTANTE: Asegúrate de que en AuthService.register uses { responseType: 'text' }
    this.authService.register(request).subscribe({
      next: (mensaje) => {
        // 1. Mostrar alerta con el mensaje que viene del backend ("Revisa tu correo...")
        alert('¡Registro exitoso! ' + mensaje);

        // 2. Cambiar a la vista de Login automáticamente
        this.esLogin = true; 
        
        // Opcional: Rellenar el email en el login para facilitar
        this.loginForm.patchValue({ email: request.email });
      },
      error: (err) => {
        console.error('Error completo:', err);
        
        // Lógica para extraer el mensaje de error correctamente
        // Si el backend envía un JSON { message: "..." }, lo sacamos de err.error.message
        // Si envía un string directo, lo sacamos de err.error
        const mensajeError = err.error?.message || err.error || 'Ocurrió un error desconocido';
        
        alert('Error al registrar: ' + mensajeError);
      }
    });
  }
  
  // Método auxiliar para alternar entre formularios desde el HTML
  toggleForm() {
    this.esLogin = !this.esLogin;
    this.error = null;
  }
}