// --- AUTENTICACIÓN ---
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  nombre: string;
  email: string;
  password: string;
  direccion: string;
}

export interface AuthResponse {
  token: string;
  email: string;
  role: string;
}

// --- NEGOCIO (Entidades Base) ---

export interface Usuario {
  idUsuario: number;
  nombre: string;
  email: string;
  direccion?: string;
  telefono?: string;
  rol?: string;
}

export interface Categoria {
  idCategoria: number;
  nombre: string;
  descripcion?: string;
}

export interface Producto {
  idProducto: number;
  sku: string;
  nombre: string;
  descripcion: string;
  precio: number;
  stock: number;
  urlImagen: string;
  ean?: string;
  activo?: boolean;
  categoria?: Categoria;
}

// --- CARRITO (¡ESTO ES LO QUE TE FALTABA!) ---

export interface ItemCarrito {
  id: number;       // O idItemCarrito, revisa cómo lo manda tu backend
  producto: Producto;
  cantidad: number;
  subtotal: number;
}

export interface Carrito {
  idCarrito: number;
  usuario: Usuario;
  items: ItemCarrito[];
  total: number;
}

// --- PEDIDOS ---

export interface ItemPedido {
  idItemPedido: number;
  cantidad: number;
  subtotal: number;
  producto: Producto;
}

export interface Pedido {
  idPedido: number;
  fecha: Date | string;
  total: number;
  estado: string; // PROCESANDO, ENVIADO, etc.
  usuario: Usuario;
  items?: ItemPedido[];
}