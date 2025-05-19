package spring.boot.api.controleFin.model.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import spring.boot.api.controleFin.model.dto.MovimentacaoDTO;
import spring.boot.api.controleFin.model.entities.Categoria;
import spring.boot.api.controleFin.model.entities.Movimentacao;
import spring.boot.api.controleFin.model.entities.Usuario;
import spring.boot.api.controleFin.model.repositories.MovimentacaoRepository;
import spring.boot.api.controleFin.model.repositories.UsuarioRepository;

import javax.xml.crypto.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovimentacaoServiceTest {

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private MovimentacaoService movimentacaoService;

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deve criar a movimentação com sucesso, com o tipo da movimentação como crédito e " +
            "com todas as validações dentro do esperado")
    void adicionarMovimentacaoCase1() {
        MovimentacaoDTO dto = new MovimentacaoDTO(
                "credito",
                LocalDate.parse("2024-04-20"),
                new BigDecimal("100.00"),
                Categoria.SALARIO,
                1L);

        Optional<Usuario> usuario =
                Optional.of(new Usuario(
                        1L,
                        "Maria",
                        "12345678900",
                        new BigDecimal("100.00")));

        when(usuarioRepository.findById(dto.getIdUsuario())).thenReturn(usuario);

        movimentacaoService.adicionarMovimentacao(dto);

        verify(movimentacaoRepository, times(1)).save(any());
        verify(usuarioRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve criar a movimentação com sucesso, com o tipo da movimentação como débito e " +
            "com todas as validações dentro do esperado")
    void adicionarMovimentacaoCase2() {
        MovimentacaoDTO dto = new MovimentacaoDTO(
                "debito",
                LocalDate.parse("2024-04-20"),
                new BigDecimal("50.00"),
                Categoria.SALARIO,
                1L);

        Optional<Usuario> usuario =
                Optional.of(new Usuario(
                        1L,
                        "Maria",
                        "12345678900",
                        new BigDecimal("100.00")));

        when(usuarioRepository.findById(dto.getIdUsuario())).thenReturn(usuario);

        movimentacaoService.adicionarMovimentacao(dto);

        verify(movimentacaoRepository, times(1)).save(any());
        verify(usuarioRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando não for passado um id válido")
    void adicionarMovimentacaoCase3() {
        MovimentacaoDTO dto = new MovimentacaoDTO(
                "credito",
                LocalDate.parse("2024-04-20"),
                new BigDecimal("100.00"),
                Categoria.SALARIO,
                1L);

        Optional<Usuario> usuario =
                Optional.of(new Usuario(
                        1L,
                        "Maria",
                        "12345678900",
                        new BigDecimal("100.00")));

        when(usuarioRepository.findById(usuario.get().getId())).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.adicionarMovimentacao(dto);
        });

        Assertions.assertEquals("Usuário não encontrado!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());
    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o tipo da operação for inválida")
    void adicionarMovimentacaoCase4() {
        MovimentacaoDTO dto = new MovimentacaoDTO(
                "cred",
                LocalDate.parse("2024-04-20"),
                new BigDecimal("100.00"),
                Categoria.ALIMENTACAO,
                1L);

        Optional<Usuario> usuario =
                Optional.of(new Usuario(
                        1L,
                        "Maria",
                        "12345678900",
                        new BigDecimal("50.00")));

        when(usuarioRepository.findById(dto.getIdUsuario())).thenReturn(usuario);

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.adicionarMovimentacao(dto);
        });

        Assertions.assertEquals("Tipo de movimentação inválido", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());
    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o usuário tentar " +
            "fazer uma movimentação de débito que deixe seu saldo negativo")
    void adicionarMovimentacaoCase5() {
        MovimentacaoDTO dto = new MovimentacaoDTO(
                "debito",
                LocalDate.parse("2024-04-20"),
                new BigDecimal("100.00"),
                Categoria.ALIMENTACAO,
                1L);

        Optional<Usuario> usuario =
                Optional.of(new Usuario(
                        1L,
                        "Maria",
                        "12345678900",
                        new BigDecimal("50.00")));

        when(usuarioRepository.findById(dto.getIdUsuario())).thenReturn(usuario);

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.adicionarMovimentacao(dto);
        });

        Assertions.assertEquals("Saldo insuficiente para esta operação", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());
    }

    @Test
    @DisplayName("Deve deletar uma movimentação de crédito com sucesso, ajustando o saldo do usuário")
    void deletarMovimentacaoCase1() {

        List<Movimentacao> movimentacoes = new ArrayList<>();
        Long idUsuario = 1L;
        Long idMovimentacao = 1L;

        Usuario usuario = new Usuario(
                        1L,
                        "Maria",
                        "12345678900",
                        movimentacoes,
                        new BigDecimal("200.00"));

        Movimentacao movimentacao = new Movimentacao(
                idMovimentacao,
                usuario,
                "credito",
                new BigDecimal("100.00"),
                LocalDate.parse("2024-04-20"),
                Categoria.SALARIO);

        usuario.getMovimentacoes().add(movimentacao);

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(movimentacaoRepository.findByIdAndUsuarioId(idMovimentacao, idUsuario)).thenReturn(Optional.of(movimentacao));

        movimentacaoService.deletarMovimentacao(idUsuario, idMovimentacao);

        verify(movimentacaoRepository, times(1)).deleteById(any());
        verify(usuarioRepository, times(1)).save(any());

        Assertions.assertEquals(new BigDecimal("100.00"), usuario.getSaldo());
    }

    @Test
    @DisplayName("Deve deletar uma movimentação de débito com sucesso, ajustando o saldo do usuário")
    void deletarMovimentacaoCase2() {

        List<Movimentacao> movimentacoes = new ArrayList<>();
        Long idUsuario = 1L;
        Long idMovimentacao = 1L;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                movimentacoes,
                new BigDecimal("200.00"));

        Movimentacao movimentacao = new Movimentacao(
                idMovimentacao,
                usuario,
                "debito",
                new BigDecimal("100.00"),
                LocalDate.parse("2024-04-20"),
                Categoria.SALARIO);

        usuario.getMovimentacoes().add(movimentacao);

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(movimentacaoRepository.findByIdAndUsuarioId(idMovimentacao, idUsuario)).thenReturn(Optional.of(movimentacao));

        movimentacaoService.deletarMovimentacao(idUsuario, idMovimentacao);

        verify(movimentacaoRepository, times(1)).deleteById(any());
        verify(usuarioRepository, times(1)).save(any());

        Assertions.assertEquals(new BigDecimal("300.00"), usuario.getSaldo());
    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o ID " +
            "do usuário não for encontrado")
    void deletarMovimentacaoCase3() {

        List<Movimentacao> movimentacoes = new ArrayList<>();
        Long idUsuario = 1L;
        Long idMovimentacao = 1L;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                movimentacoes,
                new BigDecimal("200.00"));

        Movimentacao movimentacao = new Movimentacao(
                idMovimentacao,
                usuario,
                "debito",
                new BigDecimal("100.00"),
                LocalDate.parse("2024-04-20"),
                Categoria.SALARIO);

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.deletarMovimentacao(idUsuario, idMovimentacao);
        });

        Assertions.assertEquals("Usuário não encontrado!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando a movimentação " +
            "não for encontrado")
    void deletarMovimentacaoCase4() {

        List<Movimentacao> movimentacoes = new ArrayList<>();
        Long idUsuario = 1L;
        Long idMovimentacao = 1L;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                movimentacoes,
                new BigDecimal("200.00"));

        Movimentacao movimentacao = new Movimentacao(
                idMovimentacao,
                usuario,
                "debito",
                new BigDecimal("100.00"),
                LocalDate.parse("2024-04-20"),
                Categoria.SALARIO);

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(movimentacaoRepository.findByIdAndUsuarioId(idMovimentacao, idUsuario)).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.deletarMovimentacao(idUsuario, idMovimentacao);
        });

        Assertions.assertEquals("Movimentação não encontrada ou não pertence a este usuário!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verify(movimentacaoRepository).findByIdAndUsuarioId(idMovimentacao, idUsuario);
    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o tipo da movimentação " +
            "não for válido")
    void deletarMovimentacaoCase5() {

        List<Movimentacao> movimentacoes = new ArrayList<>();
        Long idUsuario = 1L;
        Long idMovimentacao = 1L;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                movimentacoes,
                new BigDecimal("200.00"));

        Movimentacao movimentacao = new Movimentacao(
                idMovimentacao,
                usuario,
                "deb",
                new BigDecimal("100.00"),
                LocalDate.parse("2024-04-20"),
                Categoria.SALARIO);

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(movimentacaoRepository.findByIdAndUsuarioId(idMovimentacao, idUsuario)).thenReturn(Optional.of(movimentacao));

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.deletarMovimentacao(idUsuario, idMovimentacao);
        });

        Assertions.assertEquals("Tipo de movimentação inválido", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verify(movimentacaoRepository).findByIdAndUsuarioId(idMovimentacao, idUsuario);
    }

    @Test
    @DisplayName("Deve retornar com sucesso uma lista de transações filtradas pelo mes e ano")
    void buscarPorMesCase1() {

        int mes = 4;
        int ano = 2024;
        Long idUsuario = 1L;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        Movimentacao movimentacao = new Movimentacao(
                1L,
                usuario,
                "debito",
                new BigDecimal("100.00"),
                LocalDate.parse("2024-04-20"),
                Categoria.SALARIO);

        List<Movimentacao> movimentacoes = List.of(movimentacao);

        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(movimentacaoRepository.findByUsuarioIdAndDataBetween(idUsuario, inicio, fim)).thenReturn(movimentacoes);

        List<MovimentacaoDTO> resultado = movimentacaoService.buscarPorMes(mes, ano, idUsuario);

        Assertions.assertEquals(1, resultado.size());
        Assertions.assertEquals("debito", resultado.get(0).getTipoMovimentacao());
        Assertions.assertEquals(new BigDecimal("100.00"), resultado.get(0).getValor());
        Assertions.assertEquals(Categoria.SALARIO, resultado.get(0).getCategoria());

        verify(usuarioRepository).findById(idUsuario);
        verify(movimentacaoRepository).findByUsuarioIdAndDataBetween(idUsuario, inicio, fim);

    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o ID do usuário não for encontrado")
    void buscarPorMesCase2() {

        int mes = 4;
        int ano = 2024;
        Long idUsuario = 1L;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        Movimentacao movimentacao = new Movimentacao(
                1L,
                usuario,
                "debito",
                new BigDecimal("100.00"),
                LocalDate.parse("2024-04-20"),
                Categoria.SALARIO);

        List<Movimentacao> movimentacoes = List.of(movimentacao);

        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPorMes(mes, ano, idUsuario);
        });

        Assertions.assertEquals("Usuário não encontrado!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verifyNoInteractions(movimentacaoRepository);

    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o mês for passado menor que 0")
    void buscarPorMesCase3() {

        int mes = -1;
        int ano = 2024;
        Long idUsuario = 1L;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPorMes(mes, ano, idUsuario);
        });

        Assertions.assertEquals("O mês deve ser entre 1 (Janeiro) e 12 (Dezembro)", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verifyNoInteractions(movimentacaoRepository);

    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o mês for passado maior que 12")
    void buscarPorMesCase4() {

        int mes = 13;
        int ano = 2024;
        Long idUsuario = 1L;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPorMes(mes, ano, idUsuario);
        });

        Assertions.assertEquals("O mês deve ser entre 1 (Janeiro) e 12 (Dezembro)", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verifyNoInteractions(movimentacaoRepository);

    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o ano for passado maior que o ano atual")
    void buscarPorMesCase5() {

        int mes = 4;
        int ano = 2026;
        Long idUsuario = 1L;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPorMes(mes, ano, idUsuario);
        });

        Assertions.assertEquals("O ano não pode ser maior que o ano atual!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verifyNoInteractions(movimentacaoRepository);

    }

    @Test
    @DisplayName("Caso de sucesso, onde é passado um ano e id válido")
    void buscarPorAnoCase1() {
        int ano = 2024;
        Long idUsuario = 1L;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        Movimentacao movimentacao = new Movimentacao(
                1L,
                usuario,
                "debito",
                new BigDecimal("100.00"),
                LocalDate.parse("2024-04-20"),
                Categoria.SALARIO);

        List<Movimentacao> movimentacoes = List.of(movimentacao);

        LocalDate inicio = LocalDate.of(ano, 1, 1);
        LocalDate fim = LocalDate.of(ano, 12, 31);

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(movimentacaoRepository.findByUsuarioIdAndDataBetween(idUsuario, inicio, fim)).thenReturn(movimentacoes);

        List<MovimentacaoDTO> resultado = movimentacaoService.buscarPorAno(ano, idUsuario);

        Assertions.assertEquals(1, resultado.size());
        Assertions.assertEquals("debito", resultado.get(0).getTipoMovimentacao());
        Assertions.assertEquals(new BigDecimal("100.00"), resultado.get(0).getValor());
        Assertions.assertEquals(Categoria.SALARIO, resultado.get(0).getCategoria());

        verify(usuarioRepository).findById(idUsuario);
        verify(movimentacaoRepository).findByUsuarioIdAndDataBetween(idUsuario, inicio, fim);

    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o ID for inválido")
    void buscarPorAnoCase2() {

        int ano = 2024;
        Long idUsuario = 1L;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        Movimentacao movimentacao = new Movimentacao(
                1L,
                usuario,
                "debito",
                new BigDecimal("100.00"),
                LocalDate.parse("2024-04-20"),
                Categoria.SALARIO);

        List<Movimentacao> movimentacoes = List.of(movimentacao);

        LocalDate inicio = LocalDate.of(ano, 1, 1);
        LocalDate fim = LocalDate.of(ano, 12, 31);

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPorAno(ano, idUsuario);
        });

        Assertions.assertEquals("Usuário não encontrado!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verifyNoInteractions(movimentacaoRepository);

    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o ano for passado maior que o ano atual")
    void buscarPorAnoCase3() {

        int ano = 2026;
        Long idUsuario = 1L;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPorAno(ano, idUsuario);
        });

        Assertions.assertEquals("O ano não pode ser maior que o ano atual!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verifyNoInteractions(movimentacaoRepository);

    }

    @Test
    @DisplayName("Caso de sucesso, onde os parâmetros são passados de forma válida")
    void buscarPersonalizadaCase1() {
        Long idUsuario = 1L;
        int diaIni = 1;
        int mesIni = 1;
        int anoIni = 2023;
        int diaFim = 31;
        int mesFim = 5;
        int anoFim = 2025;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        Movimentacao movimentacao = new Movimentacao(
                1L,
                usuario,
                "debito",
                new BigDecimal("100.00"),
                LocalDate.parse("2024-04-20"),
                Categoria.SALARIO);

        List<Movimentacao> movimentacoes = List.of(movimentacao);

        LocalDate inicio = LocalDate.of(anoIni, mesIni, diaIni);
        LocalDate fim = LocalDate.of(anoFim, mesFim, diaFim);

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(movimentacaoRepository.findByUsuarioIdAndDataBetween(idUsuario, inicio, fim)).thenReturn(movimentacoes);

        List<MovimentacaoDTO> resultado = movimentacaoService.buscarPersonalizada(
                anoIni, anoFim, mesIni, mesFim, diaIni, diaFim, idUsuario);

        Assertions.assertEquals(1, resultado.size());
        Assertions.assertEquals("debito", resultado.get(0).getTipoMovimentacao());
        Assertions.assertEquals(new BigDecimal("100.00"), resultado.get(0).getValor());
        Assertions.assertEquals(Categoria.SALARIO, resultado.get(0).getCategoria());

        verify(usuarioRepository).findById(idUsuario);
        verify(movimentacaoRepository).findByUsuarioIdAndDataBetween(idUsuario, inicio, fim);
    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o ID for inválido")
    void buscarPersonalizadaCase2() {
        Long idUsuario = 1L;
        int diaIni = 1;
        int mesIni = 1;
        int anoIni = 2023;
        int diaFim = 31;
        int mesFim = 5;
        int anoFim = 2025;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        Movimentacao movimentacao = new Movimentacao(
                1L,
                usuario,
                "debito",
                new BigDecimal("100.00"),
                LocalDate.parse("2024-04-20"),
                Categoria.SALARIO);

        List<Movimentacao> movimentacoes = List.of(movimentacao);

        LocalDate inicio = LocalDate.of(anoIni, mesIni, diaIni);
        LocalDate fim = LocalDate.of(anoFim, mesFim, diaFim);

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPersonalizada(anoIni, anoFim, mesIni, mesFim, diaIni, diaFim, idUsuario);
        });

        Assertions.assertEquals("Usuário não encontrado!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verifyNoInteractions(movimentacaoRepository);
    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o mês inicial for menor que 1")
    void buscarPersonalizadaCase3() {
        Long idUsuario = 1L;
        int diaIni = 1;
        int mesIni = 0;
        int anoIni = 2023;
        int diaFim = 31;
        int mesFim = 5;
        int anoFim = 2025;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPersonalizada(anoIni, anoFim, mesIni, mesFim, diaIni, diaFim, idUsuario);
        });

        Assertions.assertEquals("O mês inicial deve ser entre 1 e 12!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verifyNoInteractions(movimentacaoRepository);
    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o mês inicial for maior que 12")
    void buscarPersonalizadaCase4() {
        Long idUsuario = 1L;
        int diaIni = 1;
        int mesIni = 13;
        int anoIni = 2023;
        int diaFim = 31;
        int mesFim = 5;
        int anoFim = 2025;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPersonalizada(anoIni, anoFim, mesIni, mesFim, diaIni, diaFim, idUsuario);
        });

        Assertions.assertEquals("O mês inicial deve ser entre 1 e 12!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verifyNoInteractions(movimentacaoRepository);
    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o mês final for menor que 1")
    void buscarPersonalizadaCase5() {
        Long idUsuario = 1L;
        int diaIni = 1;
        int mesIni = 1;
        int anoIni = 2023;
        int diaFim = 31;
        int mesFim = 0;
        int anoFim = 2025;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPersonalizada(anoIni, anoFim, mesIni, mesFim, diaIni, diaFim, idUsuario);
        });

        Assertions.assertEquals("O mês final deve ser entre 1 e 12!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verifyNoInteractions(movimentacaoRepository);
    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o mês final for maior que 12")
    void buscarPersonalizadaCase6() {
        Long idUsuario = 1L;
        int diaIni = 1;
        int mesIni = 1;
        int anoIni = 2023;
        int diaFim = 31;
        int mesFim = 13;
        int anoFim = 2025;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPersonalizada(anoIni, anoFim, mesIni, mesFim, diaIni, diaFim, idUsuario);
        });

        Assertions.assertEquals("O mês final deve ser entre 1 e 12!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verifyNoInteractions(movimentacaoRepository);
    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o mês inicial for maior que o mês final dentro de um mesmo ano")
    void buscarPersonalizadaCase7() {
        Long idUsuario = 1L;
        int diaIni = 1;
        int mesIni = 8;
        int anoIni = 2024;
        int diaFim = 31;
        int mesFim = 1;
        int anoFim = 2024;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPersonalizada(anoIni, anoFim, mesIni, mesFim, diaIni, diaFim, idUsuario);
        });

        Assertions.assertEquals("O mês inicial não pode ser maior que o mês final!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verifyNoInteractions(movimentacaoRepository);
    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o ano final for maior que o ano atual")
    void buscarPersonalizadaCase8() {
        Long idUsuario = 1L;
        int diaIni = 1;
        int mesIni = 1;
        int anoIni = 2023;
        int diaFim = 31;
        int mesFim = 12;
        int anoFim = 2026;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPersonalizada(anoIni, anoFim, mesIni, mesFim, diaIni, diaFim, idUsuario);
        });

        Assertions.assertEquals("O ano final não pode ser maior que o ano atual!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verifyNoInteractions(movimentacaoRepository);
    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o ano inicial for maior que o ano final")
    void buscarPersonalizadaCase9() {
        Long idUsuario = 1L;
        int diaIni = 1;
        int mesIni = 1;
        int anoIni = 2025;
        int diaFim = 01;
        int mesFim = 01;
        int anoFim = 2024;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPersonalizada(anoIni, anoFim, mesIni, mesFim, diaIni, diaFim, idUsuario);
        });

        Assertions.assertEquals("O ano inicial não pode ser maior que o ano final!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verifyNoInteractions(movimentacaoRepository);
    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o dia inicial for menor que 1")
    void buscarPersonalizadaCase10() {
        Long idUsuario = 1L;
        int diaIni = 0;
        int mesIni = 1;
        int anoIni = 2023;
        int diaFim = 01;
        int mesFim = 01;
        int anoFim = 2025;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPersonalizada(anoIni, anoFim, mesIni, mesFim, diaIni, diaFim, idUsuario);
        });

        Assertions.assertEquals("O dia inicial não pode ser menor que 1!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verifyNoInteractions(movimentacaoRepository);
    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o dia final for maior que o último dia do mês")
    void buscarPersonalizadaCase11() {
        Long idUsuario = 1L;
        int diaIni = 1;
        int mesIni = 1;
        int anoIni = 2023;
        int diaFim = 31;
        int mesFim = 02;
        int anoFim = 2025;
        int ultimoDiaDoMesFim = LocalDate.of(anoFim, mesFim, 1).lengthOfMonth();

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPersonalizada(anoIni, anoFim, mesIni, mesFim, diaIni, diaFim, idUsuario);
        });

        Assertions.assertEquals("O dia final não pode ser maior que " + ultimoDiaDoMesFim + " para " + mesFim + "/" + anoFim, excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verifyNoInteractions(movimentacaoRepository);
    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o dia inicial for maior que o dia final dentro de um mesmo mês e ano")
    void buscarPersonalizadaCase12() {
        Long idUsuario = 1L;
        int diaIni = 20;
        int mesIni = 1;
        int anoIni = 2024;
        int diaFim = 10;
        int mesFim = 1;
        int anoFim = 2024;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPersonalizada(anoIni, anoFim, mesIni, mesFim, diaIni, diaFim, idUsuario);
        });

        Assertions.assertEquals("O dia inicial não pode ser maior que o dia final!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verifyNoInteractions(movimentacaoRepository);
    }

    @Test
    @DisplayName("Caso de sucesso, onde os parâmetros são passados de forma válida")
    void buscarPorCategoriaCase1() {

        Long idUsuario = 1L;
        Categoria categoria = Categoria.SALARIO;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        Movimentacao movimentacao = new Movimentacao(
                1L,
                usuario,
                "debito",
                new BigDecimal("100.00"),
                LocalDate.parse("2024-04-20"),
                Categoria.SALARIO);

        List<Movimentacao> movimentacoes = List.of(movimentacao);

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(movimentacaoRepository.findByUsuarioIdAndCategoria(idUsuario, categoria)).thenReturn(movimentacoes);

        List<MovimentacaoDTO> resultado = movimentacaoService.buscarPorCategoria(idUsuario, categoria);

        Assertions.assertEquals(1, resultado.size());
        Assertions.assertEquals("debito", resultado.get(0).getTipoMovimentacao());
        Assertions.assertEquals(new BigDecimal("100.00"), resultado.get(0).getValor());
        Assertions.assertEquals(Categoria.SALARIO, resultado.get(0).getCategoria());

        verify(usuarioRepository).findById(idUsuario);
        verify(movimentacaoRepository).findByUsuarioIdAndCategoria(idUsuario, categoria);

    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o id for inválido")
    void buscarPorCategoriaCase2() {

        Long idUsuario = 1L;
        Categoria categoria = Categoria.SALARIO;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));


        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPorCategoria(idUsuario, categoria);
        });

        Assertions.assertEquals("Usuário não encontrado!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verifyNoInteractions(movimentacaoRepository);
    }

    @Test
    @DisplayName("Caso de sucesso, onde o ID do usuário e ID da movimentação são passados de forma válida")
    void buscarPorIdCase1() {

        Long idUsuario = 1L;
        Long idMovimentacao = 1L;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        Movimentacao movimentacao = new Movimentacao(
                idMovimentacao,
                usuario,
                "debito",
                new BigDecimal("100.00"),
                LocalDate.parse("2024-04-20"),
                Categoria.SALARIO);

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(movimentacaoRepository.findByIdAndUsuarioId(idMovimentacao, idUsuario)).thenReturn(Optional.of(movimentacao));

        MovimentacaoDTO dto = movimentacaoService.buscarPorId(idMovimentacao, idUsuario);

        Assertions.assertEquals(1L, movimentacao.getId());
        Assertions.assertEquals("debito", movimentacao.getTipoMovimentacao());
        Assertions.assertEquals(new BigDecimal("100.00"), movimentacao.getValor());
        Assertions.assertEquals(Categoria.SALARIO, movimentacao.getCategoria());

        verify(usuarioRepository).findById(idUsuario);
        verify(movimentacaoRepository).findByIdAndUsuarioId(idMovimentacao, idUsuario);

    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o id do usuário for inválido")
    void buscarPorIdCase2() {

        Long idUsuario = 1L;
        Long idMovimentacao = 1L;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        Movimentacao movimentacao = new Movimentacao(
                idMovimentacao,
                usuario,
                "debito",
                new BigDecimal("100.00"),
                LocalDate.parse("2024-04-20"),
                Categoria.SALARIO);

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPorId(idMovimentacao, idUsuario);
        });

        Assertions.assertEquals("Usuário não encontrado!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verifyNoInteractions(movimentacaoRepository);

    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o id da movimentação for inválido")
    void buscarPorIdCase3() {

        Long idUsuario = 1L;
        Long idMovimentacao = 1L;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        Movimentacao movimentacao = new Movimentacao(
                idMovimentacao,
                usuario,
                "debito",
                new BigDecimal("100.00"),
                LocalDate.parse("2024-04-20"),
                Categoria.SALARIO);

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(movimentacaoRepository.findByIdAndUsuarioId(idMovimentacao, idUsuario)).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPorId(idMovimentacao, idUsuario);
        });

        Assertions.assertEquals("Movimentação não encontrada ou não pertence a este usuário!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);
        verify(movimentacaoRepository).findByIdAndUsuarioId(idMovimentacao, idUsuario);

    }

    @Test
    @DisplayName("Caso de sucesso, onde o ID do usuário é passado de forma válida")
    void buscarPorIdUsuarioCase1() {

        List<Movimentacao> movimentacoes = new ArrayList<>();
        Long idUsuario = 1L;
        Long idMovimentacao = 1L;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                movimentacoes,
                new BigDecimal("200.00"));

        Movimentacao movimentacao = new Movimentacao(
                idMovimentacao,
                usuario,
                "debito",
                new BigDecimal("100.00"),
                LocalDate.parse("2024-04-20"),
                Categoria.SALARIO);

        usuario.getMovimentacoes().add(movimentacao);

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        List<MovimentacaoDTO> dtoList = movimentacaoService.buscarPorIdUsuario(idUsuario);

        Assertions.assertEquals(1L, movimentacao.getId());
        Assertions.assertEquals("debito", movimentacao.getTipoMovimentacao());
        Assertions.assertEquals(new BigDecimal("100.00"), movimentacao.getValor());
        Assertions.assertEquals(Categoria.SALARIO, movimentacao.getCategoria());

        verify(usuarioRepository).findById(idUsuario);

    }

    @Test
    @DisplayName("Deve lançar uma exceção ResponseStatusException quando o id do usuário for inválido")
    void buscarPorIdUsuarioCase2() {

        Long idUsuario = 1L;

        Usuario usuario = new Usuario(
                idUsuario,
                "Maria",
                "12345678900",
                new ArrayList<>(),
                new BigDecimal("200.00"));

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () ->{
            movimentacaoService.buscarPorIdUsuario(idUsuario);
        });

        Assertions.assertEquals("Usuário não encontrado!", excecao.getReason());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());

        verify(usuarioRepository).findById(idUsuario);

    }
}