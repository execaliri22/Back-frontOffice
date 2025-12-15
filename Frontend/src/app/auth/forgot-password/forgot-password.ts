import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service'; // Ajusta la ruta si es necesario
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './forgot-password.html',
  styleUrls: ['../../pages/auth/auth.component.css']
})
export class ForgotPasswordComponent {
  form: FormGroup;
  message: string | null = null;
  error: string | null = null;
  loading = false;

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
        this.message = res || 'Si el correo existe, hemos enviado un enlace.';
        this.form.disable();
      },
      error: (err) => {
        this.loading = false;
        console.error(err);
        this.error = 'Error al conectar con el servidor.';
      }
    });
  }
}