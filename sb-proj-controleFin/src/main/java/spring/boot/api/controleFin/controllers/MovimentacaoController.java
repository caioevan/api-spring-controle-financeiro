package spring.boot.api.controleFin.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import spring.boot.api.controleFin.model.dto.MovimentacaoDTO;
import spring.boot.api.controleFin.model.entities.Categoria;
import spring.boot.api.controleFin.model.services.MovimentacaoService;

@RestController
@RequestMapping("/controle-fin/movimentacao")
public class MovimentacaoController {

	@Autowired
	MovimentacaoService service;
	
	//Recebe um DTO de movimentação como requisição e salva no banco de dados
	@PostMapping
	public ResponseEntity<Void> adicionarMovimentacao(@RequestBody	MovimentacaoDTO dto){
		service.adicionarMovimentacao(dto);
		return ResponseEntity.ok().build();
	}
	
	//Recebe uma lista de movimentações para ser adicionada no banco de dados
	@PostMapping("/lote")
	public ResponseEntity<Void> adicionarMovimentacoes(@RequestBody List<MovimentacaoDTO> movimentacoes) {
	    movimentacoes.forEach(service::adicionarMovimentacao);
	    return ResponseEntity.ok().build();
	}
	
	//Deleta uma movimentação
	@DeleteMapping
	public ResponseEntity<Void> deletarMovimentacao(
			@RequestParam Long idUsuario, 
			@RequestParam Long idMovimentacao){
		service.deletarMovimentacao(idUsuario, idMovimentacao);
		return ResponseEntity.ok().build();
	}
	
	//Busca as movimentações de cada usuário
	@GetMapping
	public ResponseEntity<List<MovimentacaoDTO>> buscarPorIdUsuario(@RequestParam Long id) {
		return ResponseEntity.ok(service.buscarPorIdUsuario(id));
	}
	
	//Busca a movimentação do id passado do usuário passado
	@GetMapping("/busca-por-id")
	public ResponseEntity<MovimentacaoDTO> buscarPorId(
			@RequestParam Long idMovimentacao, 
			@RequestParam Long idUsuario){
		return ResponseEntity.ok(service.buscarPorId(idMovimentacao, idUsuario));
	}
	
	//Busca as movimentações de cada usuário por mês
	@GetMapping("/busca-por-mes")
	public ResponseEntity<List<MovimentacaoDTO>> buscarPorMes (
			@RequestParam int mes,
			@RequestParam int ano,
			@RequestParam Long id){
		return ResponseEntity.ok(service.buscarPorMes(mes, ano, id));
	}
	
	//Busca as movimentações de cada usuário por ano
	@GetMapping("/busca-por-ano")
	public ResponseEntity<List<MovimentacaoDTO>> buscarPorAno (
			@RequestParam int ano,
			@RequestParam Long id){
		return ResponseEntity.ok(service.buscarPorAno(ano, id));
	}
	
	//Busca as movimentações de cada usuário por categoria
	@GetMapping("/busca-por-categoria")
	public ResponseEntity<List<MovimentacaoDTO>> buscarPorCategoria (
			@RequestParam Long id,
			@RequestParam Categoria categoria){
		return ResponseEntity.ok(service.buscarPorCategoria(id, categoria));
	}
	
	//Busca as movimentações de cada usuário pelas datas especificadas na requisição
	@GetMapping("/busca-personalizada")
	public ResponseEntity<List<MovimentacaoDTO>> buscarPersonalizada (
			@RequestParam int anoIni,
			@RequestParam int anoFim,
			@RequestParam int mesIni,
			@RequestParam int mesFim,
			@RequestParam int diaIni,
			@RequestParam int diaFim,
			@RequestParam Long id){
		return ResponseEntity.ok(service.buscarPersonalizada(anoIni, anoFim, mesIni, mesFim, diaIni, diaFim, id));
	}
	
}
