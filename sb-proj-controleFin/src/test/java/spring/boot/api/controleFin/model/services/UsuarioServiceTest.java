package spring.boot.api.controleFin.model.services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import spring.boot.api.controleFin.model.dto.UsuarioDTO;
import spring.boot.api.controleFin.model.entities.Usuario;
import spring.boot.api.controleFin.model.repositories.UsuarioRepository;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @InjectMocks
    private UsuarioService usuarioService;

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deve criar o usuário com sucesso, com todas as validações dentro do esperado")
    void criarUsuarioCase1() {

        UsuarioDTO dto = new UsuarioDTO("Maria", "12345678900", new BigDecimal("100.00"));

        when(repository.existsByDoc(dto.getDoc())).thenReturn(false);
        when(repository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)); // Retorna o mesmo objeto salvo

        UsuarioDTO resultado = usuarioService.criarUsuario(dto);

        assertEquals("Maria", resultado.getNome());
        assertEquals("12345678900", resultado.getDoc());
        assertEquals(new BigDecimal("100.00"), resultado.getSaldo());

        verify(repository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando for passado um documento com menos que 11 dígitos")
    void criarUsuarioCase2() {

        UsuarioDTO dto = new UsuarioDTO("Maria", "123456789", new BigDecimal("100.00"));

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> {
           usuarioService.criarUsuario(dto);
        });

        assertEquals("Documento não pode ter menos que 11 caracteres! (11122233344)", excecao.getReason());

        assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());
    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o documento já existir")
    void criarUsuarioCase3() {

        UsuarioDTO dto = new UsuarioDTO("Maria", "12345678900", new BigDecimal("100.00"));

        when(repository.existsByDoc(dto.getDoc())).thenReturn(true);

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.criarUsuario(dto);
        });

        assertEquals("Documento já cadastrado", excecao.getReason());

        assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar a lista de usuários convertidos para DTO")
    void getUsuariosCase1() {
        List<Usuario> usuarios = List.of(
                new Usuario(1L, "Maria", "12345678900", new BigDecimal("100.00")),
                new Usuario(2L, "João", "98765432100", new BigDecimal("200.00"))
        );

        when(repository.findAll()).thenReturn(usuarios);

        List<UsuarioDTO> resultado = usuarioService.getUsuarios();

        assertEquals(2, resultado.size());
        assertEquals("Maria", resultado.get(0).getNome());
        assertEquals("João", resultado.get(1).getNome());

        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar o saldo com sucesso, levando em conta que foi passado um ID que existe na DB")
    void getSaldoCase1() {
        Optional<Usuario> usuario = Optional.of(new Usuario(1L, "Maria", "12345678900", new BigDecimal("100.00")));

        when(repository.findById(usuario.get().getId())).thenReturn(usuario);

        BigDecimal resultado = usuarioService.getSaldo(1L);

        assertEquals(new BigDecimal("100.00"), resultado);

        verify(repository, times(1)).findById(any());
    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o ID não for encontrado")
    void getSaldoCase2() {
        Optional<Usuario> usuario = Optional.of(new Usuario(1L, "Maria", "12345678900", new BigDecimal("100.00")));

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> {
           usuarioService.getSaldo(2L);
        });

        assertEquals("Usuário não encontrado", excecao.getReason());

        assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());
    }
}