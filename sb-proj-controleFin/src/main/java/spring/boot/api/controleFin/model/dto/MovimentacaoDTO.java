package spring.boot.api.controleFin.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import spring.boot.api.controleFin.model.entities.Categoria;


@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MovimentacaoDTO {

	@NotBlank
	private String tipoMovimentacao;
	
	@NotBlank
	private LocalDate data;
	
	@NotNull
	@PositiveOrZero
	private BigDecimal valor;
	
	@NotBlank
	private Categoria categoria;
	
	@NotNull
	private Long idUsuario;
	
}
