package spring.boot.api.controleFin.model.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import spring.boot.api.controleFin.model.dto.MovimentacaoDTO;
import spring.boot.api.controleFin.model.entities.Categoria;
import spring.boot.api.controleFin.model.entities.Movimentacao;
import spring.boot.api.controleFin.model.entities.Usuario;
import spring.boot.api.controleFin.model.repositories.MovimentacaoRepository;
import spring.boot.api.controleFin.model.repositories.UsuarioRepository;

@Service
public class MovimentacaoService {

	@Autowired
	MovimentacaoRepository movimentacaoRepository;

	@Autowired
	UsuarioRepository usuarioRepository;
	
	//Méetodo que adiciona uma movimentação
	public void adicionarMovimentacao(MovimentacaoDTO dto) {
		
		Usuario usuario = this.validarUsuario(dto.getIdUsuario());
		
		Movimentacao movimentacao = new Movimentacao();
		
		movimentacao.setCategoria(dto.getCategoria());
		movimentacao.setData(dto.getData());
		movimentacao.setTipoMovimentacao(dto.getTipoMovimentacao());
		movimentacao.setValor(dto.getValor());
		movimentacao.setUsuario(usuario);
		
		String tipo = dto.getTipoMovimentacao().toLowerCase();
		// Validação para debitar o valor da movimentação se o tipo for "debito", e creditar se for "credito"
		switch (tipo) {
		    case "debito":
		        usuario.debitar(movimentacao.getValor());
		        if (usuario.getSaldo().compareTo(BigDecimal.ZERO) < 0) { //Validação para negar uma operação de débito que deixe o saldo negativo
		            usuario.creditar(movimentacao.getValor());
		            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Saldo insuficiente para esta operação");
		        }
		        break;
		    case "credito":
		        usuario.creditar(movimentacao.getValor());
		        break;
		    default:
		        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de movimentação inválido");
		}
		
		movimentacaoRepository.save(movimentacao);
		usuarioRepository.save(usuario);
		
	}
	
	//Método para deletar uma movimentação
	public void deletarMovimentacao(Long idUsuario, Long idMovimentacao) {
		
	    Usuario usuario = this.validarUsuario(idUsuario);

	    //Valida se o id da movimentação existe dentro do usuário expecífico e salva na variável
	    Movimentacao movimentacao = this.validarMovimentacao(idMovimentacao, idUsuario);

	    //Aqui ajusta o saldo do usuário quando deletar a movimentação: Caso for de "debito", credita o valor no saldo, caso for de "credito", debita o valor no saldo

		String tipo = movimentacao.getTipoMovimentacao().toLowerCase();

		switch (tipo){
			case "debito":
				usuario.creditar(movimentacao.getValor());
				break;
			case "credito":
				usuario.debitar(movimentacao.getValor());
				break;
			default:
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de movimentação inválido");
		}

	    movimentacaoRepository.deleteById(idMovimentacao);
	    //Remove da lista de movimentações do objeto Usuário para não haver uma falta de compatibilidade com o banco de dados
	    usuario.getMovimentacoes().removeIf(m -> m.getId().equals(idMovimentacao)); 
	    usuarioRepository.save(usuario);
	}
	
	//Método que retorna as movimentações de um mês específico, recebendo o mês, o ano e o id do usuário
	public List<MovimentacaoDTO> buscarPorMes(int mes, int ano, Long idUsuario) {

		//Verifica se o usuário é válido
		Usuario usuario = this.validarUsuario(idUsuario);

		if(mes <= 0 || mes > 12){// Valida o mês passado pelo usuário
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O mês deve ser entre 1 (Janeiro) e 12 (Dezembro)");
		}else if(ano > LocalDate.now().getYear()){// Valida o ano passado pelo usuário
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ano não pode ser maior que o ano atual!");
		}

		//Data de início da busca, sempre começando pelo dia 1
		LocalDate inicio = LocalDate.of(ano, mes, 1);
		//Data do fim da busca, pegando o ultimo dia do mês que especificamos na data de início
		LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());
		
		//Utilizamos a Query que fizemos no UsuarioRepository para fazer a busca no banco de dados
		//É feito uma stream para mapear cada movimentação trazida da consulta do banco e transforma-las em DTO, depois coletar cada uma e retornar como uma lista de DTO
		return movimentacaoRepository.findByUsuarioIdAndDataBetween(idUsuario, inicio, fim).stream()
				.map(m -> new MovimentacaoDTO(m.getTipoMovimentacao(), m.getData(), m.getValor(), m.getCategoria(), m.getUsuario().getId()))
				.collect(Collectors.toList());
		
	}
	
	//Método que retorna as movimentações de um mês específico, recebendo o ano e o id do usuário
	public List<MovimentacaoDTO> buscarPorAno(int ano, Long idUsuario) {

		//Verifica se o usuário é válido
		Usuario usuario = this.validarUsuario(idUsuario);

		if(ano > LocalDate.now().getYear()){// Valida o ano passado pelo usuário
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ano não pode ser maior que o ano atual!");
		}

		//Data de início, com o mês 1 e dia 1
		LocalDate inicio = LocalDate.of(ano, 1, 1);
		//Data do fim, com o mês 12 e dia 31
		LocalDate fim = LocalDate.of(ano, 12, 31);
		
		//Mesmo retorno do método acima, mudando apenas as datas para consulta no banco.
		return movimentacaoRepository.findByUsuarioIdAndDataBetween(idUsuario, inicio, fim).stream()
					.map(m -> new MovimentacaoDTO(m.getTipoMovimentacao(), m.getData(), m.getValor(), m.getCategoria(), m.getUsuario().getId()))
					.collect(Collectors.toList());
		
	}
	
	//Método que retorna as movimentações dentro de um range de datas especificadas pelo usuário.
	public List<MovimentacaoDTO> buscarPersonalizada(int anoIni, int anoFim, int mesIni, int mesFim, int diaIni, int diaFim, Long idUsuario) {

		//Verifica se o usuário é válido
		Usuario usuario = this.validarUsuario(idUsuario);

		validarParametrosDeData(anoIni, anoFim, mesIni, mesFim, diaIni, diaFim);

		LocalDate inicio = LocalDate.of(anoIni, mesIni, diaIni);
		LocalDate fim = LocalDate.of(anoFim, mesFim, diaFim);
		
		return movimentacaoRepository.findByUsuarioIdAndDataBetween(idUsuario, inicio, fim).stream()
				.map(m -> new MovimentacaoDTO(m.getTipoMovimentacao(), m.getData(), m.getValor(), m.getCategoria(), m.getUsuario().getId()))
				.collect(Collectors.toList());
		
	}
	
	//Método que retorna as movimentações por categoria de cada usuário
	public List<MovimentacaoDTO> buscarPorCategoria(Long idUsuario, Categoria categoria){

		//Verifica se o usuário é válido
		Usuario usuario = this.validarUsuario(idUsuario);

		//Utilizamos a query que fizemos no UsuariorRepository
		return movimentacaoRepository.findByUsuarioIdAndCategoria(idUsuario, categoria).stream()
				.map(m -> new MovimentacaoDTO(m.getTipoMovimentacao(), m.getData(), m.getValor(), m.getCategoria(), m.getUsuario().getId()))
				.collect(Collectors.toList());
	}
	
	//Método que busca uma movimentação específica por sua id de cada usuário
	public MovimentacaoDTO buscarPorId(Long idMovimentacao, Long idUsuario) {

		//Verifica se o usuário é válido
		Usuario usuario = this.validarUsuario(idUsuario);
		Movimentacao movimentacao = this.validarMovimentacao(idMovimentacao, idUsuario);
		
		return new MovimentacaoDTO(
				movimentacao.getTipoMovimentacao(), 
				movimentacao.getData(), 
				movimentacao.getValor(), 
				movimentacao.getCategoria(), 
				movimentacao.getUsuario().getId());
		
	}
	
	//Método que retorna todas as movimentações de cada usuário
	public List<MovimentacaoDTO> buscarPorIdUsuario(Long idUsuario) {
		
		Usuario usuario = this.validarUsuario(idUsuario);
		
		return usuario.getMovimentacoes().stream()
				.map(m -> new MovimentacaoDTO(m.getTipoMovimentacao(), m.getData(), m.getValor(), m.getCategoria(), m.getUsuario().getId()))
				.collect(Collectors.toList());
	}

	private Usuario validarUsuario(Long idUsuario){
		return usuarioRepository.findById(idUsuario).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não encontrado!"));
	}

	private Movimentacao validarMovimentacao(Long idMovimentacao, Long idUsuario){
		return movimentacaoRepository.findByIdAndUsuarioId(idMovimentacao, idUsuario).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Movimentação não encontrada ou não pertence a este usuário!"));
	}

	private void validarParametrosDeData(int anoIni, int anoFim, int mesIni, int mesFim, int diaIni, int diaFim) {

		if (mesIni < 1 || mesIni > 12) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O mês inicial deve ser entre 1 e 12!");
		}
		if (mesFim < 1 || mesFim > 12) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O mês final deve ser entre 1 e 12!");
		}
		if (anoIni == anoFim && mesIni > mesFim) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O mês inicial não pode ser maior que o mês final!");
		}
		if (anoIni > anoFim) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ano inicial não pode ser maior que o ano final!");
		}
		if (anoFim > LocalDate.now().getYear()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ano final não pode ser maior que o ano atual!");
		}
		if (diaIni < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O dia inicial não pode ser menor que 1!");
		}

		int ultimoDiaDoMesFim = LocalDate.of(anoFim, mesFim, 1).lengthOfMonth();

		if (diaFim > ultimoDiaDoMesFim) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"O dia final não pode ser maior que " + ultimoDiaDoMesFim + " para " + mesFim + "/" + anoFim);
		}
		if (anoIni == anoFim && mesIni == mesFim && diaIni > diaFim) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O dia inicial não pode ser maior que o dia final!");
		}
	}
	
}
