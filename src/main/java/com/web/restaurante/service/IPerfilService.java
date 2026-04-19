package com.web.restaurante.service;

import com.web.restaurante.model.Opcion;
import com.web.restaurante.model.Perfil;

import java.util.List;
import java.util.Optional;

public interface IPerfilService {
    List<Perfil> listar();
    Optional<Perfil> obtenerPorId(Long id);
    Optional<Perfil> encontrarPorPerfil(String perfil);


    Perfil guardar(Perfil perfil);
    Optional<Perfil> alternarEstado(Long id);

    void eliminar(Long id);

    List<Opcion> listarOpciones();

}
