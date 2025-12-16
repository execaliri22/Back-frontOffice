import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="auth-container">
      <div class="auth-card">
        <h2>Nueva Contraseña</h2>
        
        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Ingresa tu nueva clave</label>
            <input type="password" formControlName="password" placeholder="Mínimo 6 caracteres">
          </div>

          <button type="submit" [disabled]="form.invalid || loading">
            {{ loading ? 'Actualizando...' : 'Cambiar Contraseña' }}
          </button>
        </form>

        <div *ngIf="message" class="success-msg">
            ✅ {{ message }}
            <p><a routerLink="/auth">Iniciar Sesión</a></p>
        </div>
        <div *ngIf="error" class="error-msg">❌ {{ error }}</div>
      </div>
    </div>
  `,
  styles: [`
    .auth-container { display: flex; justify-content: center; padding-top: 50px; }
    .auth-card { background: #1a1a1a; padding: 2rem; border-radius: 8px; color: white; width: 400px; border: 1px solid #333; }
    input { width: 100%; padding: 10px; margin: 10px 0; background: #333; border: 1px solid #555; color: white; border-radius: 4px; }
    button { width: 100%; padding: 10px; background: #00f2ff; border: none; font-weight: bold; cursor: pointer; margin-top: 10px; border-radius: 4px; }
    .success-msg { color: #4caf50; margin-top: 10px; text-align: center; }
    .error-msg { color: #f44336; margin-top: 10px; }
    a { color: #00f2ff; text-decoration: none; }
  `]
})
export class ResetPasswordComponent implements OnInit {
  form: FormGroup;
  token = '';
  loading = false;
  message: string | null = null;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private authService: AuthService
  ) {
    this.form = this.fb.group({
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit() {
    // Obtener el token de la URL
    this.token = this.route.snapshot.queryParams['token'];
    if (!this.token) {
      this.error = 'Token no válido. Por favor solicita un nuevo enlace.';
      this.form.disable();
    }
  }

  onSubmit() {
    if (this.form.invalid || !this.token) return;
    this.loading = true;
    this.error = null;

this.authService.resetPassword(this.token, this.form.value.password).subscribe({
      next: (res: any) => {  // <--- AGREGADO EL TIPO
        this.loading = false;
        this.message = res; 
        this.form.disable();
      },
      error: (err: any) => { // <--- AGREGADO EL TIPO
        this.loading = false;
        // Ahora sí podemos acceder a propiedades internas sin error
        this.error = err.error?.message || err.error || 'Error al restablecer la contraseña.';
      }
    });
  }
}