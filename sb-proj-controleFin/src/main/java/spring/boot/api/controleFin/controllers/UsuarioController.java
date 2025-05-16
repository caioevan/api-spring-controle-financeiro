package spring.boot.api.controleFin.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import spring.boot.api.controleFin.model.dto.UsuarioDTO;
import spring.boot.api.controleFin.model.services.UsuarioService;

@RestController
@RequestMapping("/controle-fin/usuario")
public class UsuarioController {

	@Autowired
	UsuarioService service;
	
	//Recebe um DTO da requisição e adiciona o usuário no banco
	@PostMapping
	public ResponseEntity<UsuarioDTO> adicionarUsuario(@RequestBody @Valid UsuarioDTO dto){
		UsuarioDTO resultado = service.criarUsuario(dto);
		return ResponseEntity.status(HttpStatus.OK).body(resultado);
	}
	
	//Retorna uma lista dos usuários cadastrados
	@GetMapping
	public ResponseEntity<List<UsuarioDTO>> getUsuario() {
		List<UsuarioDTO> usuarios = service.getUsuarios();
		return ResponseEntity.ok(usuarios);
	}
	
	//Retorna o saldo de um usuário específico
	@GetMapping("/saldo")
	public BigDecimal getSaldo(@RequestParam Long id) {
		return service.getSaldo(id);
	}
	
	
}
