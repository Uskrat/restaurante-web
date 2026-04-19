package com.web.restaurante.controller;

import com.web.restaurante.model.Opcion;
import com.web.restaurante.model.Usuario;
import com.web.restaurante.serviceImpl.UsuarioServiceImpl;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
public class LoginController {
    private final UsuarioServiceImpl usuarioService;

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("logout", "Has cerrado sesión exitosamente.");
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String mostrarFormularioLogin(HttpSession session) {
        if (session.getAttribute("usuarioLogueado") != null) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String usuario, @RequestParam String clave, HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Optional<Usuario> existente = usuarioService.encontrarPorUsuario(usuario);

        if (existente.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
            return "redirect:/login";
        }

        Usuario usuarioEncontrado = existente.get();

        if (usuarioEncontrado.getEstado() != 1) {
            redirectAttributes.addFlashAttribute("error", "Este usuario se encuentra inactivo.");
            return "redirect:/login";
        }

        if (usuarioService.verificarClave(clave, usuarioEncontrado.getClave())) {
            session.setAttribute("usuarioLogueado", usuarioEncontrado);
            Set<String> permisos = new HashSet<>();
            usuarioEncontrado.getPerfil().getOpciones().forEach(opcion -> {
                permisos.add(opcion.getRuta());
            });
            session.setAttribute("permisosUsuario", permisos);
            List<Opcion> opcionesMenu = usuarioEncontrado.getPerfil().getOpciones().stream()
                    .sorted(Comparator.comparing(Opcion::getId))
                    .collect(Collectors.toList());
            Map<String, List<Opcion>> menuAgrupado = new LinkedHashMap<>();
            List<Opcion> opcionesIndependientes = new ArrayList<>();
            for (Opcion opcion : opcionesMenu) {
                String[] partesRuta = opcion.getRuta().split("/");
                if (partesRuta.length > 2) {
                    String grupo = partesRuta[1];
                    String nombreGrupo = grupo.substring(0, 1).toUpperCase() + grupo.substring(1);
                    menuAgrupado.computeIfAbsent(nombreGrupo, k -> new ArrayList<>()).add(opcion);
                } else {
                    opcionesIndependientes.add(opcion);
                }
            }

            session.setAttribute("menuAgrupado", menuAgrupado);
            session.setAttribute("opcionesIndependientes", opcionesIndependientes);
            session.setAttribute("menuOpciones", opcionesMenu);

            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("error", "Contraseña incorrecta.");
            return "redirect:/login";
        }
    }
}
