package com.web.restaurante.serviceImpl;

import com.web.restaurante.model.Usuario;
import com.web.restaurante.repository.UsuarioRepository;
import com.web.restaurante.service.IUsuarioService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAllByEstadoNot(2);
    }

    @Override
    @Transactional
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> encontrarPorUsuario(String usuario) {
        return usuarioRepository.findByUsuario(usuario);
    }

    @Override
    public Usuario guardar(Usuario usuario) {

        // Si existe (actualizar)
        if (usuario.getId() != null) {
            Usuario existente = usuarioRepository.findById(usuario.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado para actualizar"));

            if (esUsuarioDuplicado(usuario.getUsuario(), usuario.getId())) throw new IllegalArgumentException(
                    "Este usuario ya está en uso por otra cuenta activa."
            );

            if (esCorreoDuplicado(usuario.getCorreo(), usuario.getId())) throw new IllegalArgumentException(
                    "El correo ya está en uso por otra cuenta activa."
            );

            existente.setNombre(usuario.getNombre());
            existente.setUsuario(usuario.getUsuario());
            existente.setCorreo(usuario.getCorreo());

            if (usuario.getClave() != null && !usuario.getClave().trim().isEmpty()) {
                existente.setClave(passwordEncoder.encode(usuario.getClave().trim()));
            }

            return usuarioRepository.save(existente);
        }
        // Si es nuevo (crear)

        if (esUsuarioDuplicado(usuario.getUsuario(), null)) throw new IllegalArgumentException(
                "Este usuario ya está en uso por otra cuenta activa."
        );

        if (esCorreoDuplicado(usuario.getCorreo(), null)) throw new IllegalArgumentException(
                "El correo ya está en uso por otra cuenta activa."
        );

        // Encripta la clave
        if (usuario.getClave() == null || usuario.getClave().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "La clave es obligatoria"
            );
        }
        usuario.setClave(passwordEncoder.encode(usuario.getClave().trim()));

        return usuarioRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> alternarEstado(Long id) {
        return Optional.empty();
    }

    @Override
    public void eliminar(Long id) {

    }

    @Override
    public long contar() {
        return 0;
    }

    @Override
    public boolean verificarClave(String claveTextoPlano, String claveEncriptada) {
        return false;
    }

    private boolean esUsuarioDuplicado(String usuario, Long id) {
        Optional<Usuario> existente = usuarioRepository.findByUsuarioIgnoreCase(usuario);

        if (existente.isEmpty()) return false;

        Usuario u = existente.get();

        boolean esActivo = u.getEstado() != 2;
        boolean esMismoId = id != null && u.getId().equals(id);

        return esActivo && !esMismoId;
    }

    private boolean esCorreoDuplicado(String correo, Long id) {
        Optional<Usuario> existente = usuarioRepository.findByCorreoIgnoreCase(correo);

        if (existente.isEmpty()) return false;

        Usuario u = existente.get();

        boolean esActivo = u.getEstado() != 2;
        boolean esMismoId = id != null && u.getId().equals(id);

        return esActivo && !esMismoId;
    }
}
