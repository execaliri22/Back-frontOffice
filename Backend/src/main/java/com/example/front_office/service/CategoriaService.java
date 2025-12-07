package com.example.front_office.service;

import com.example.front_office.model.Categoria;
import com.example.front_office.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    // Obtener todas las categorías (para listar en el panel)
    public List<Categoria> findAll() {
        return categoriaRepository.findAll();
    }

    // Buscar una por ID (útil para la edición)
    public Optional<Categoria> findById(Integer id) {
        return categoriaRepository.findById(id);
    }

    // Guardar (Crear o Actualizar)
    @Transactional
    public Categoria save(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    // Eliminar
    @Transactional
    public void deleteById(Integer id) {
        // Opcional: Podrías verificar si tiene productos asociados antes de borrar
        // para evitar errores de llave foránea, pero por ahora esto funciona.
        categoriaRepository.deleteById(id);
    }
}