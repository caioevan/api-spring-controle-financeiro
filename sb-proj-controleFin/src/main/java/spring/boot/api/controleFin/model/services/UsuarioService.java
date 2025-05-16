package spring.boot.api.controleFin.model.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import spring.boot.api.controleFin.model.dto.UsuarioDTO;
import spring.boot.api.controleFin.model.entities.Usuario;
import spring.boot.api.controleFin.model.repositories.UsuarioRepository;

@Service
public class UsuarioService {

	@Autowired
	UsuarioRepository repository;

	//Método para criar um usuário
	public UsuarioDTO criarUsuario(UsuarioDTO dto) {
		
		//Validação se o documento não tem menos que 11 dígitos e se não está cadastrado na base de dados;
		if(dto.getDoc().length() < 11) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Documento não pode ter menos que 11 caracteres! (11122233344)");
		}else if(repository.existsByDoc(dto.getDoc())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Documento já cadastrado");
		}
		
		Usuario usuario = new Usuario(dto.getNome(), dto.getDoc(), dto.getSaldo());
		repository.save(usuario);
		
		return new UsuarioDTO(usuario.getNome(), usuario.getDoc(), usuario.getSaldo());
	}
	
	//Método para fazer uma busca dos usuários cadastrados
	public List<UsuarioDTO> getUsuarios() {
	    return repository.findAll().stream()
	            .map(u -> new UsuarioDTO(u.getNome(), u.getDoc(), u.getSaldo()))
	            .collect(Collectors.toList());
	}
	
	//Método que retorna o saldo de um usuário expecífico
	public BigDecimal getSaldo(Long id) {
		Usuario usuario = repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não encontrado"));
		
		return usuario.getSaldo();
	}
}
