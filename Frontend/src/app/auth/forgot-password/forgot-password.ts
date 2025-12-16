import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="auth-container">
      <div class="auth-card">
        <h2>Recuperar Contraseña</h2>
        <p>Ingresa tu correo y te enviaremos un enlace para restablecer tu clave.</p>

        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Email</label>
            <input type="email" formControlName="email" placeholder="ejemplo@correo.com">
          </div>

          <button type="submit" [disabled]="form.invalid || loading">
            {{ loading ? 'Enviando...' : 'Enviar Enlace' }}
          </button>
        </form>

        <div *ngIf="message" class="success-msg">✅ {{ message }}</div>
        <div *ngIf="error" class="error-msg">❌ {{ error }}</div>

        <a routerLink="/auth" class="back-link">Volver al Login</a>
      </div>
    </div>
  `,
  styles: [`
    .auth-container { display: flex; justify-content: center; padding-top: 50px; }
    .auth-card { background: #1a1a1a; padding: 2rem; border-radius: 8px; color: white; width: 400px; border: 1px solid #333; }
    input { width: 100%; padding: 10px; margin: 10px 0; background: #333; border: 1px solid #555; color: white; border-radius: 4px; }
    button { width: 100%; padding: 10px; background: #00f2ff; border: none; font-weight: bold; cursor: pointer; margin-top: 10px; border-radius: 4px; }
    button:disabled { background: #555; cursor: not-allowed; }
    .success-msg { color: #4caf50; margin-top: 10px; }
    .error-msg { color: #f44336; margin-top: 10px; }
    .back-link { display: block; margin-top: 15px; color: #aaa; text-align: center; text-decoration: none; font-size: 0.9rem; }
  `]
})
export class ForgotPasswordComponent {
  form: FormGroup;
  loading = false;
  message: string | null = null;
  error: string | null = null;

  constructor(private fb: FormBuilder, private authService: AuthService) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit() {
    if (this.form.invalid) return;
    this.loading = true;
    this.message = null;
    this.error = null;

    this.authService.forgotPassword(this.form.value.email).subscribe({
      next: (res) => {
        this.loading = false;
        this.message = res; // "Se ha enviado un enlace..."
        this.form.disable(); // Evitar doble envío
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || err.error || 'No se pudo enviar el correo.';
      }
    });
  }
}