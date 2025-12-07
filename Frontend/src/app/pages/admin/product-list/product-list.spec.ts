import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing'; // <--- Importante para simular backend
import { ProductListComponent } from './product-list'; // <--- Nombre correcto de la clase y archivo

describe('ProductListComponent', () => { // <--- Nombre correcto del grupo de tests
  let component: ProductListComponent;
  let fixture: ComponentFixture<ProductListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      // Importamos el componente (porque es standalone)
      // Y HttpClientTestingModule para que no falle el AdminService
      imports: [ProductListComponent, HttpClientTestingModule] 
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});