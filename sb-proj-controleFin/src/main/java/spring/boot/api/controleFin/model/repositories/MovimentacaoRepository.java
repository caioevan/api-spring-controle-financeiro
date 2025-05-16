package spring.boot.api.controleFin.model.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import spring.boot.api.controleFin.model.entities.Categoria;
import spring.boot.api.controleFin.model.entities.Movimentacao;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

	//Query de busca por usuário entre uma data inicial e uma data final
	public List<Movimentacao> findByUsuarioIdAndDataBetween(Long idUsuario, LocalDate inicio, LocalDate fim);
	
	//Query de usca por usuário e pela categoria
	public List<Movimentacao> findByUsuarioIdAndCategoria(Long idUsuario, Categoria categoria);
	
	//Query de busca por usuário e movimentação expecífica
	public Optional<Movimentacao> findByIdAndUsuarioId(Long idMovimentacao, Long idUsuario);
	
}
