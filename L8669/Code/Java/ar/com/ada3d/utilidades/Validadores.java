package ar.com.ada3d.utilidades;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

public class Validadores {

	
	 /** 
	  * Validación del número de CUIT / CUIL. 
	  * @param number Número a validar. 
	  * @throws DocumentException cuando el número de CUIT / CUIL no es 
	  * válido. Salvo que sea 0 (cero) que no lo valido 
	  */ 
	 
	public void validateCUIT0(FacesContext facesContext, UIComponent component,
			Object value) {

		String number = value.toString();
		if (number.equals("0"))
			return;
		

		if (!validacionCUIT(number)) {
			FacesMessage message = new FacesMessage("El CUIT es inválido.");
			throw new ValidatorException(message);
		}
	}
	
	 /** 
	  * Validación del número de CUIT / CUIL. 
	  * @param number Número a validar. 
	  * @throws DocumentException cuando el número de CUIT / CUIL no es 
	  * válido.  
	  */
	public void validateCUIT(FacesContext facesContext, UIComponent component,
			Object value) {
		if (!validacionCUIT(value.toString())) {
			FacesMessage message = new FacesMessage("El CUIT es inválido.");
			throw new ValidatorException(message);
		}
	}
	
	private boolean validacionCUIT(String number){
		boolean res = false;

		if (number != null && number.trim().length() != 0) {
			number = number.trim();
			try {
				int[] magicValues = { 5, 4, 3, 2, 7, 6, 5, 4, 3, 2 };
				int[] values = new int[11];
				int i;
				int sum = 0;

				number = number.replace("-", "");

				if (number.length() == 11) {
					for (i = 0; i < 11; i++)
						values[i] = Integer
								.parseInt(number.substring(i, i + 1));

					int checkDigit = values[10];

					for (i = 0; i < 10; i++)
						sum = sum + values[i] * magicValues[i];

					int dividend = sum / 11;
					int product = dividend * 11;
					int substraction = sum - product;
					checkDigit = (substraction > 0) ? 11 - substraction
							: substraction;

					res = (checkDigit == values[i]);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return res;
	}
  
	 /** 
	  * Validación del número de Factura. 
	  * @param number Número a validar. 
	  * @throws DocumentException cuando el número de factura no es 
	  * válido. Salvo que sea 0 (cero) que no lo valido 
	  */ 
	 
	public void validateNumeroFactura(FacesContext facesContext, UIComponent component,
			Object value) {
		//TODO: Vamos a validar si carga 2 veces la misma factura?
		String number = value.toString();
		if (number.equals("0"))
			return;
		
		
		if (!(number.length() == 13)) {
			FacesMessage message = new FacesMessage("El número de factura debe ser con formato '0000-00000000'.");
			throw new ValidatorException(message);
		}
	}
}
