import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductFormComponent } from './product-form'; // Asegúrate que el nombre del archivo coincida
import { AdminService } from '../../../core/services/admin.service';
import { CategoriaService } from '../../../core/services/categoria.service';
import { Router, ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

describe('ProductFormComponent', () => {
  let component: ProductFormComponent;
  let fixture: ComponentFixture<ProductFormComponent>;

  // Creamos espías (Mocks) para simular los servicios
  const adminServiceMock = {
    getProductos: jasmine.createSpy('getProductos').and.returnValue(of([])),
    createProducto: jasmine.createSpy('createProducto').and.returnValue(of({})),
    updateProducto: jasmine.createSpy('updateProducto').and.returnValue(of({}))
  };

  const categoriaServiceMock = {
    getCategorias: jasmine.createSpy('getCategorias').and.returnValue(of([]))
  };

  const routerMock = {
    navigate: jasmine.createSpy('navigate')
  };

  const activatedRouteMock = {
    paramMap: of({ get: (key: string) => null }) // Simulamos que no hay ID (modo creación)
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductFormComponent], // Es standalone, va en imports
      providers: [
        { provide: AdminService, useValue: adminServiceMock },
        { provide: CategoriaService, useValue: categoriaServiceMock },
        { provide: Router, useValue: routerMock },
        { provide: ActivatedRoute, useValue: activatedRouteMock }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('debe inicializar el formulario como inválido si está vacío', () => {
    expect(component.form.valid).toBeFalse();
  });
  
  it('debe validar que el precio no sea negativo', () => {
    const precioControl = component.form.get('precio');
    precioControl?.setValue(-10);
    expect(precioControl?.valid).toBeFalse();
  });
});