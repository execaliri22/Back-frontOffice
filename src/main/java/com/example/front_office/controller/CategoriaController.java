package com.example.front_office.controller;

import com.example.front_office.model.Categoria; // Asegúrate que la ruta sea correcta
import com.example.front_office.repository.CategoriaRepository; // Asegúrate que la ruta sea correcta
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Importa todas las anotaciones de web

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categorias") // Ruta base para este controlador
public class CategoriaController {

    @Autowired // Inyecta la dependencia del repositorio
    private CategoriaRepository categoriaRepository;

    // --- GET (Leer) ---

    /**
     * Obtiene todas las categorías.
     * Responde a: GET /api/categorias
     * @return Lista de todas las categorías y estado 200 OK.
     */
    @GetMapping
    public List<Categoria> listarTodasLasCategorias() {
        return categoriaRepository.findAll(); // Busca todas las entidades Categoria
    }

    /**
     * Obtiene una categoría específica por su ID.
     * Responde a: GET /api/categorias/{id} (ej. /api/categorias/5)
     * @param id El ID de la categoría a buscar (viene de la URL).
     * @return La categoría encontrada con estado 200 OK, o estado 404 Not Found si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> obtenerCategoriaPorId(@PathVariable Integer id) {
        Optional<Categoria> categoriaOptional = categoriaRepository.findById(id); // Busca por ID

        // Forma concisa de devolver 200 si existe, 404 si no
        return categoriaOptional.map(ResponseEntity::ok) // Si 'categoriaOptional' tiene valor, lo envuelve en ResponseEntity.ok()
                               .orElse(ResponseEntity.notFound().build()); // Si está vacío, construye un ResponseEntity 404
    }

    // --- POST (Crear) ---

    /**
     * Crea una nueva categoría.
     * Responde a: POST /api/categorias
     * Espera un cuerpo JSON como: { "nombre": "Nombre De Categoria" }
     * @param nuevaCategoria El objeto Categoria mapeado desde el cuerpo JSON de la solicitud.
     * @return La categoría recién creada con su ID asignado y estado 201 Created.
     */
    @PostMapping
    public ResponseEntity<Categoria> crearNuevaCategoria(@RequestBody Categoria nuevaCategoria) {
        // Aseguramos que el ID sea nulo para que JPA genere uno nuevo
        nuevaCategoria.setIdCategoria(null);
        Categoria categoriaGuardada = categoriaRepository.save(nuevaCategoria); // Guarda la nueva entidad
        // Devuelve el estado 201 (Created) y la entidad guardada en el cuerpo
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaGuardada);
    }

    // --- PUT (Actualizar) ---

    /**
     * Actualiza una categoría existente identificada por su ID.
     * Responde a: PUT /api/categorias/{id} (ej. /api/categorias/5)
     * Espera un cuerpo JSON como: { "nombre": "Nombre Actualizado" }
     * @param id El ID de la categoría a actualizar (viene de la URL).
     * @param categoriaActualizada El objeto Categoria con los nuevos datos (viene del cuerpo JSON).
     * @return La categoría actualizada con estado 200 OK, o estado 404 Not Found si no existe.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizarCategoriaExistente(@PathVariable Integer id, @RequestBody Categoria categoriaActualizada) {
        return categoriaRepository.findById(id) // Busca si existe la categoría
                .map(categoriaExistente -> { // Si existe...
                    // Actualiza los campos necesarios (solo 'nombre' en este caso)
                    categoriaExistente.setNombre(categoriaActualizada.getNombre()); // Asume que Categoria tiene setters
                    Categoria guardada = categoriaRepository.save(categoriaExistente); // Guarda los cambios
                    // Devuelve 200 OK con la entidad actualizada
                    return ResponseEntity.ok(guardada);
                })
                .orElse(ResponseEntity.notFound().build()); // Si no existe, devuelve 404
    }

    // --- DELETE (Borrar) ---

    /**
     * Elimina una categoría existente identificada por su ID.
     * Responde a: DELETE /api/categorias/{id} (ej. /api/categorias/5)
     * @param id El ID de la categoría a eliminar (viene de la URL).
     * @return Estado 204 No Content si se eliminó correctamente, o 404 Not Found si no existía.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoriaPorId(@PathVariable Integer id) {
        if (!categoriaRepository.existsById(id)) { // Verifica si existe antes de intentar borrar
            return ResponseEntity.notFound().build(); // Devuelve 404 si no existe
        }
        categoriaRepository.deleteById(id); // Elimina la entidad
        // Devuelve 204 No Content (respuesta vacía indicando éxito)
        return ResponseEntity.noContent().build();
    }
}