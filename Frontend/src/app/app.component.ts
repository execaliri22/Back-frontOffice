import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit { // Implementamos OnInit
  
  // Inyecciones
  public authService = inject(AuthService);
  private router = inject(Router);

  // Estado del menú (Signal)
  menuVisible = signal<boolean>(false);
  
  // Bandera para saber si estamos en el panel de admin
  esAdminPanel: boolean = false;

  ngOnInit() {
    // 1. Verificar la ruta actual al cargar la app
    this.verificarRuta();

    // 2. Suscribirse a los cambios de navegación
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.verificarRuta();
      this.menuVisible.set(false); // Cerramos el menú al navegar
    });
  }

  verificarRuta() {
    // Si la URL contiene '/admin', activamos la bandera
    this.esAdminPanel = this.router.url.includes('/admin');
  }

  // Acción de alternar menú
  toggleMenu() {
    this.menuVisible.update(value => !value);
  }

  // Cerrar sesión
  logout() {
    this.authService.logout();
    this.menuVisible.set(false);
    this.router.navigate(['/auth']);
  }

  getUserInitials(): string {
    const name = this.authService.currentUserName();
    if (!name) return 'US';
    
    const parts = name.split(' ');
    if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
    
    return (parts[0][0] + parts[1][0]).toUpperCase();
  }
}
