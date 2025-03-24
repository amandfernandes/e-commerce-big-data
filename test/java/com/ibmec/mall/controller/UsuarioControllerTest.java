package com.ibmec.mall.controller;

import com.ibmec.mall.model.Usuario;
import com.ibmec.mall.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Usuário Teste");
        usuario.setEmail("teste@email.com");
        usuario.setSenha("senha123");
        usuario.setEndereco("Endereço Teste");
        usuario.setTelefone("123456789");
    }

    @Test
    void criarUsuario_DeveRetornarUsuarioCriado() {
        when(usuarioService.criarUsuario(any(Usuario.class))).thenReturn(usuario);

        ResponseEntity<Usuario> response = usuarioController.criarUsuario(usuario);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(usuario.getId(), response.getBody().getId());
        assertEquals(usuario.getNome(), response.getBody().getNome());
        assertEquals(usuario.getEmail(), response.getBody().getEmail());
        verify(usuarioService).criarUsuario(usuario);
    }

    @Test
    void buscarPorId_DeveRetornarUsuario() {
        when(usuarioService.buscarPorId(1L)).thenReturn(usuario);

        ResponseEntity<Usuario> response = usuarioController.buscarPorId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(usuario.getId(), response.getBody().getId());
        assertEquals(usuario.getNome(), response.getBody().getNome());
        assertEquals(usuario.getEmail(), response.getBody().getEmail());
        verify(usuarioService).buscarPorId(1L);
    }

    @Test
    void listarTodos_DeveRetornarListaDeUsuarios() {
        List<Usuario> usuarios = Arrays.asList(usuario);
        when(usuarioService.listarTodos()).thenReturn(usuarios);

        ResponseEntity<List<Usuario>> response = usuarioController.listarTodos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(usuario.getId(), response.getBody().get(0).getId());
        assertEquals(usuario.getNome(), response.getBody().get(0).getNome());
        verify(usuarioService).listarTodos();
    }

    @Test
    void atualizarUsuario_DeveRetornarUsuarioAtualizado() {
        when(usuarioService.atualizarUsuario(eq(1L), any(Usuario.class))).thenReturn(usuario);

        ResponseEntity<Usuario> response = usuarioController.atualizarUsuario(1L, usuario);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(usuario.getId(), response.getBody().getId());
        assertEquals(usuario.getNome(), response.getBody().getNome());
        assertEquals(usuario.getEmail(), response.getBody().getEmail());
        verify(usuarioService).atualizarUsuario(1L, usuario);
    }

    @Test
    void deletarUsuario_DeveRetornarNoContent() {
        ResponseEntity<Void> response = usuarioController.deletarUsuario(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(usuarioService).deletarUsuario(1L);
    }
} 