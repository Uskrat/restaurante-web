package com.web.restaurante.controller;

import com.web.restaurante.serviceImpl.UsuarioServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class DashboardController {
    private final UsuarioServiceImpl usuarioService;

    @GetMapping("/dashboard")
    public String mostrarPagina(Model model) {
        long totalUsuarios = usuarioService.contar();
        model.addAttribute("totalUsuarios", totalUsuarios);

        return "dashboard";
    }
}
