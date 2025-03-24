package com.ibmec.mall.controller;

import com.ibmec.mall.config.TestConfig;
import com.ibmec.mall.model.Usuario;
import com.ibmec.mall.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        
        usuario = new Usuario();
        usuario.setNome("Usuário Teste");
        usuario.setEmail("teste@email.com");
        usuario.setSenha("senha123");
        usuario.setEndereco("Endereço Teste");
        usuario.setTelefone("123456789");
        usuario = usuarioRepository.save(usuario);
    }

    @Test
    void criarUsuario_DeveRetornarUsuarioCriado() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Novo Usuário\",\"email\":\"novo@email.com\",\"senha\":\"senha456\",\"endereco\":\"Novo Endereço\",\"telefone\":\"987654321\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Novo Usuário")))
                .andExpect(jsonPath("$.email", is("novo@email.com")));
    }

    @Test
    void buscarPorId_DeveRetornarUsuario() throws Exception {
        mockMvc.perform(get("/api/usuarios/{id}", usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(usuario.getId().intValue())))
                .andExpect(jsonPath("$.nome", is(usuario.getNome())))
                .andExpect(jsonPath("$.email", is(usuario.getEmail())));
    }

    @Test
    void listarTodos_DeveRetornarListaDeUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(usuario.getId().intValue())))
                .andExpect(jsonPath("$[0].nome", is(usuario.getNome())));
    }

    @Test
    void atualizarUsuario_DeveRetornarUsuarioAtualizado() throws Exception {
        mockMvc.perform(put("/api/usuarios/{id}", usuario.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Usuário Atualizado\",\"email\":\"atualizado@email.com\",\"senha\":\"senha789\",\"endereco\":\"Endereço Atualizado\",\"telefone\":\"111222333\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(usuario.getId().intValue())))
                .andExpect(jsonPath("$.nome", is("Usuário Atualizado")))
                .andExpect(jsonPath("$.email", is("atualizado@email.com")));
    }

    @Test
    void deletarUsuario_DeveRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/api/usuarios/{id}", usuario.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/usuarios/{id}", usuario.getId()))
                .andExpect(status().isNotFound());
    }
} 