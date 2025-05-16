package spring.boot.api.controleFin.model.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

	@NotBlank
	private String nome;
	
	@NotBlank
	private String doc;
	
	@NotNull
	@PositiveOrZero
	private BigDecimal saldo;
	
}
