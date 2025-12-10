import { ComponentFixture, TestBed } from '@angular/core/testing';

// CORRECCIÓN: Importamos OrderListComponent (no OrderList)
import { OrderListComponent } from './order-list';

describe('OrderListComponent', () => {
  let component: OrderListComponent;
  let fixture: ComponentFixture<OrderListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      // Importamos el componente correcto
      imports: [OrderListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrderListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});