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
    if (this.loginForm.invalid) return;
    
    this.error = null;
    const request: LoginRequest = this.loginForm.value;

    // 1. CASO ADMIN: Usamos loginAdmin y vamos al Dashboard
    if (request.email === 'admin@admin.com') {
      
      this.authService.loginAdmin(request).subscribe({
        next: () => {
          console.log('Login Admin Exitoso');
          this.router.navigate(['/admin']); // <--- Redirige al Dashboard
        },
        error: (err) => {
          console.error(err);
          this.error = 'Error de Admin: Credenciales incorrectas.';
        }
      });

    } else {
      // 2. CASO CLIENTE: Usamos login (normal) y vamos a la Tienda
      // ¡Aquí estaba el error! Antes llamabas a loginAdmin también aquí.
      this.authService.login(request).subscribe({
        next: () => {
          console.log('Login Cliente Exitoso');
          this.router.navigate(['/tienda']); // <--- Redirige a la Tienda
        },
        error: (err) => {
          console.error(err);
          this.error = 'Email o contraseña incorrectos.';
        }
      });
    }
  }

  onRegister() {
    if (this.registerForm.invalid) return;
    this.error = null;
    const request: RegisterRequest = this.registerForm.value;
    
    this.authService.register(request).subscribe({
      next: () => this.router.navigate(['/tienda']),
      error: (err) => this.error = 'Error al registrar. ¿El email ya existe?'
    });
  }
}