package spring.boot.api.controleFin.model.entities;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="usuarios")
public class Usuario {

	/*
	 * 
	 * Nome;
	 * Lista de movimentação;
	 * Saldo;
	 * 
	 */
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //id do usuário
	
	@Column(length = 50)
	private String nome; //nome do usuário
	
	@Column(length = 11, unique = true)
	private String doc; //documento do usuário
	
	@OneToMany(mappedBy = "usuario", fetch = FetchType.EAGER)
	private List<Movimentacao> movimentacoes; //Lista de movimentações (1 usuário pode ter mais de uma movimentação)
	
	private BigDecimal saldo; //Saldo do usuário
	
	public Usuario (String nome, String doc, BigDecimal saldo) { //Construtor que recebe o nome, documento e saldo do DTO
		this.nome = nome;
		this.doc = doc;
		this.saldo = saldo;
	}
	
	public void creditar(BigDecimal valor) { //Método para creditar um valor vindo de uma movimentação
	    this.saldo = this.saldo.add(valor);
	}

	public void debitar(BigDecimal valor) { //Método para debitar um valor vindo de uma movimentação
	    this.saldo = this.saldo.subtract(valor);
	}
	
}
