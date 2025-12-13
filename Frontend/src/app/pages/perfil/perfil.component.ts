import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { CommonModule, NgOptimizedImage, DatePipe, CurrencyPipe } from '@angular/common'; // Añadidos Pipes
import { HttpClient } from '@angular/common/http'; // Añadido HttpClient

// Validador personalizado para contraseñas
function passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
  const nueva = control.get('nueva');
  const confirmar = control.get('confirmar');
  return nueva && confirmar && nueva.value && confirmar.value && nueva.value !== confirmar.value ? { noCoinciden: true } : null;
}

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgOptimizedImage, DatePipe, CurrencyPipe], // Añadidos DatePipe y CurrencyPipe
  templateUrl: './perfil.component.html',
  styleUrls: ['./perfil.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PerfilComponent implements OnInit {
  // --- INYECCIÓN DE DEPENDENCIAS ---
  authService = inject(AuthService);
  private fb = inject(FormBuilder);
  private http = inject(HttpClient); // Inyectamos HttpClient para los pedidos

  // --- VARIABLES DE PEDIDOS ---
  private baseUrl = 'http://localhost:8080/api'; // URL del Backend
  public pedidos = signal<any[]>([]); // Signal para la lista de pedidos
  public cargandoPedidos = signal(true);

  // --- VARIABLES DE PERFIL ---
  nombreForm!: FormGroup;
  contraForm!: FormGroup;

  // Signals para estados de UI
  guardandoNombre = signal(false);
  guardandoContra = signal(false);
  subiendoFoto = signal(false);
  eliminandoFoto = signal(false);
  
  errorNombre = signal<string | null>(null);
  errorContra = signal<string | null>(null);
  errorFoto = signal<string | null>(null);
  mensajeExito = signal<string | null>(null);

  selectedFile: File | null = null;
  objectUrl = signal<string | null>(null);

  ngOnInit(): void {
    // 1. Inicializar Formularios
    this.nombreForm = this.fb.group({
      nombre: [this.authService.currentUserName() || '', Validators.required]
    });

    this.contraForm = this.fb.group({
      actual: ['', Validators.required],
      nueva: ['', [Validators.required, Validators.minLength(6)]],
      confirmar: ['', Validators.required]
    }, { validators: passwordsMatchValidator });

    // 2. Cargar Pedidos
    this.cargarPedidos();
  }

  // --- LÓGICA DE PEDIDOS (NUEVO) ---
  cargarPedidos(): void {
    this.cargandoPedidos.set(true);
    this.http.get<any[]>(`${this.baseUrl}/pedidos/mis-pedidos`).subscribe({
      next: (data) => {
        // Ordenamos del más nuevo al más viejo
        const ordenados = data.sort((a, b) => b.idPedido - a.idPedido);
        this.pedidos.set(ordenados);
        this.cargandoPedidos.set(false);
      },
      error: (err) => {
        console.error('Error cargando pedidos', err);
        this.cargandoPedidos.set(false);
      }
    });
  }

  // --- LÓGICA DE PERFIL (TU CÓDIGO EXISTENTE) ---

  getUserInitials(): string {
    const name = this.authService.currentUserName();
    if (!name) return '??';
    const parts = name.trim().split(' ').filter(part => part.length > 0);
    if (parts.length >= 2) return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    if (parts.length === 1 && name.length >= 2) return name.substring(0, 2).toUpperCase();
    if (parts.length === 1 && name.length === 1) return name.toUpperCase();
    return '??';
  }

  onFileSelected(event: Event): void {
     const input = event.target as HTMLInputElement;
     if (input.files && input.files[0]) {
       this.selectedFile = input.files[0];
       if (this.objectUrl()) URL.revokeObjectURL(this.objectUrl()!);
       this.objectUrl.set(URL.createObjectURL(this.selectedFile));
       this.subirFoto(); 
       input.value = ''; 
     } else {
         this.selectedFile = null;
         this.objectUrl.set(null);
     }
  }

   subirFoto(): void {
     if (!this.selectedFile) return;
     this.subiendoFoto.set(true);
     this.errorFoto.set(null);
     this.mensajeExito.set(null);

     this.authService.subirFotoPerfil(this.selectedFile).subscribe({
       next: (response) => {
         this.mensajeExito.set('Foto de perfil actualizada.');
         this.selectedFile = null;
         this.objectUrl.set(null);
       },
       error: (err: Error) => {
           this.errorFoto.set(err.message || 'Error al subir la foto.');
           this.objectUrl.set(null);
       },
       complete: () => this.subiendoFoto.set(false)
     });
   }

   eliminarFoto(): void {
      if (!confirm('¿Estás seguro de que quieres eliminar tu foto de perfil?')) return;
      this.eliminandoFoto.set(true);
      this.errorFoto.set(null);
      this.mensajeExito.set(null);

      this.authService.eliminarFotoPerfil().subscribe({
         next: (response) => {
            this.mensajeExito.set('Foto de perfil eliminada.');
            this.objectUrl.set(null);
         },
         error: (err: Error) => this.errorFoto.set(err.message || 'Error al eliminar foto.'),
         complete: () => this.eliminandoFoto.set(false)
      });
   }

  actualizarNombre(): void {
    if (this.nombreForm.invalid || this.guardandoNombre()) return;
    this.guardandoNombre.set(true);
    this.errorNombre.set(null);
    this.mensajeExito.set(null);

    const nuevoNombre = this.nombreForm.value.nombre.trim();
    this.authService.actualizarNombre(nuevoNombre).subscribe({
      next: (response) => {
         this.mensajeExito.set('Nombre actualizado correctamente.');
         this.nombreForm.reset({ nombre: nuevoNombre });
         this.nombreForm.markAsPristine();
      },
      error: (err: Error) => this.errorNombre.set(err.message || 'Error al actualizar nombre.'),
      complete: () => this.guardandoNombre.set(false)
    });
  }

  cambiarContrasena(): void {
    if (this.contraForm.invalid || this.guardandoContra()) return;
    this.guardandoContra.set(true);
    this.errorContra.set(null);
    this.mensajeExito.set(null);

    const { actual, nueva } = this.contraForm.value;
    this.authService.cambiarContrasena(actual, nueva).subscribe({
      next: () => {
         this.mensajeExito.set('Contraseña cambiada correctamente.');
         this.contraForm.reset();
         this.contraForm.markAsPristine();
         this.contraForm.markAsUntouched();
      },
      error: (err: Error) => this.errorContra.set(err.message || 'Error al cambiar contraseña.'),
      complete: () => this.guardandoContra.set(false)
    });
  }
}