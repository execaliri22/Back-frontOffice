import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-verify',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="verify-container">
      <div class="card">
        <h2>Verificación de Cuenta</h2>
        
        <p *ngIf="loading">Verificando tu cuenta, por favor espera...</p>

        <div *ngIf="message && !error" class="success">
          <p>✅ {{ message }}</p>
          <button (click)="irALogin()">Ir a Iniciar Sesión</button>
        </div>

        <div *ngIf="error" class="error">
          <p>❌ {{ error }}</p>
          <button (click)="irALogin()">Volver</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .verify-container { display: flex; justify-content: center; padding-top: 50px; }
    .card { background: #1a1a1a; padding: 2rem; border-radius: 8px; color: white; text-align: center; border: 1px solid #333; }
    button { background: #00f2ff; border: none; padding: 10px 20px; cursor: pointer; font-weight: bold; margin-top: 15px; border-radius: 4px; }
    .success { color: #4caf50; }
    .error { color: #f44336; }
  `]
})
export class VerifyComponent implements OnInit {
  loading = true;
  message: string | null = null;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    // Capturamos el token de la URL (?token=...)
    this.route.queryParams.subscribe(params => {
      const token = params['token'];
      
      if (token) {
        this.verificarToken(token);
      } else {
        this.loading = false;
        this.error = 'No se encontró el token de verificación.';
      }
    });
  }

  verificarToken(token: string) {
    this.authService.verifyAccount(token).subscribe({
      next: (msg) => {
        this.loading = false;
        this.message = msg; // "Cuenta verificada con éxito..."
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error || 'El token es inválido o ha expirado.';
      }
    });
  }

  irALogin() {
    this.router.navigate(['/auth']);
  }
}