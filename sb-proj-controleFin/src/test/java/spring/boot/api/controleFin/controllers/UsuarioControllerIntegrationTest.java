package spring.boot.api.controleFin.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import spring.boot.api.controleFin.model.entities.Usuario;
import spring.boot.api.controleFin.model.repositories.MovimentacaoRepository;
import spring.boot.api.controleFin.model.repositories.UsuarioRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve criar o usuário com sucesso")
    @Transactional
    void deveCriarUsuario() throws Exception{
        Map<String, Object> payload = Map.of(
                "nome", "Maria",
                "doc", "12345678900",
                "saldo", new BigDecimal("100.00")
        );

        mockMvc.perform(post("/controle-fin/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andDo(print())
                .andExpect(status().isOk());

        Usuario maria = usuarioRepository.getReferenceById(1L);
        Assertions.assertEquals(1L, maria.getId());
        Assertions.assertEquals("Maria", maria.getNome());
        Assertions.assertEquals("12345678900", maria.getDoc());
        Assertions.assertEquals(new BigDecimal("100.00"), maria.getSaldo());
    }

    @Test
    @DisplayName("Deve retornar 400 ao tentar adicionar um documento inválido(menos que 11 dígitos)")
    @Transactional
    void deveRetornarErroDocInvalido() throws Exception{

        Map<String, Object> payload = Map.of(
                "nome", "Maria",
                "doc", "123456789",
                "saldo", new BigDecimal("100.00")
        );

        mockMvc.perform(post("/controle-fin/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 ao tentar adicionar um documento que já existe")
    @Transactional
    void deveRetornarErroDocDuplicado() throws Exception{

        usuarioRepository.save(new Usuario("João", "12345678900", new BigDecimal("100.00")));

        Map<String, Object> payload = Map.of(
                "nome", "Maria",
                "doc", "12345678900",
                "saldo", new BigDecimal("100.00")
        );

        mockMvc.perform(post("/controle-fin/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 200 e uma lista dos usuários cadastrados")
    @Transactional
    void deveRetornarListaUsuarios() throws Exception {
        usuarioRepository.save(new Usuario("João", "12345678900", new BigDecimal("100.00")));
        usuarioRepository.save(new Usuario("Maria", "12345678901", new BigDecimal("100.00")));

        mockMvc.perform(get("/controle-fin/usuario"))
                .andDo(print())
                .andExpect(status().isOk());
        List<Usuario> usuarios = usuarioRepository.findAll();

        Assertions.assertEquals(2, usuarios.size());
        Assertions.assertEquals("12345678900", usuarios.get(0).getDoc());
        Assertions.assertEquals("12345678901", usuarios.get(1).getDoc());
    }

    @Test
    @DisplayName("Deve retornar 200 e uma lista vazia caso não tenha usuários cadastrados")
    @Transactional
    void deveRetornarListaVaziaUsuarios() throws Exception {

        mockMvc.perform(get("/controle-fin/usuario"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 200 e o saldo do usuário passado")
    @Transactional
    void deveRetornarSaldoUsuario() throws Exception{

        Usuario joao = usuarioRepository.save(new Usuario("João", "12345678900", new BigDecimal("100.00")));

        mockMvc.perform(get("/controle-fin/usuario/saldo")
                        .param("id", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        Optional<Usuario> resposta = usuarioRepository.findById(1L);

        Assertions.assertEquals(joao.getSaldo(), resposta.get().getSaldo());
    }

    @Test
    @DisplayName("Deve retornar 400 quando for passado ID inválido")
    @Transactional
    void deveRetornarErroIdInvalido() throws Exception{

        Usuario joao = usuarioRepository.save(new Usuario("João", "12345678900", new BigDecimal("100.00")));

        mockMvc.perform(get("/controle-fin/usuario/saldo")
                        .param("id", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
