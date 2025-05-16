package spring.boot.api.controleFin.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import spring.boot.api.controleFin.model.entities.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
	
	// Query de busca para retornar um true ou false se existe ou não um dacumento na base de dados
	boolean existsByDoc(String documento);

}
