package spring.boot.api.controleFin.model.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
@Table(name="movimentacoes")
public class Movimentacao {

	/*
	 * Tipo - créito ou débito
	 * Valor
	 * Data
	 * Categoria
	 */
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // id da movimentaçao
	
	@ManyToOne
	private Usuario usuario; // O usuário que pertence a esta movimentação (1 usuário pode ter várias movimentações e uma movimentação pertence a 1 usuário)
	
	private String tipoMovimentacao; // Tipo da movimentação (crédito ou débito)
	
	private BigDecimal valor; // Valor da movimentação
	
	private LocalDate data; // Data da movimentação
	
	@Enumerated(EnumType.STRING)
	@Column(length = 40)
	private Categoria categoria; // Cateegoria da movimentação, todas as categorias estão no Enum Categoria.java
	
}
