package ar.com.ada3d.utilidades;

public class Validadores {

	
	
	 /** 
  * Validación del número de CUIT / CUIL. 
  * @param number Número a validar. 
  * @throws DocumentException cuando el número de CUIT / CUIL no es 
  * válido. 
  */ 
 @SuppressWarnings("unused")
private void validateCUIT(String number) { 
  boolean res = false; 
 
  if (number != null && number.trim().length() != 0) { 
   number = number.trim(); 
   try { 
    int[] magicValues = {5,4,3,2,7,6,5,4,3,2}; 
    int[] values = new int[11]; 
    int i; 
    int sum = 0; 
 
    number = number.replace("-", ""); 
 
    if (number.length() == 11) { 
     for (i = 0; i < 11; i++) 
      values[i] = Integer.parseInt(number.substring(i, i+1)); 
 
     int checkDigit = values[10]; 
 
     for (i = 0; i < 10; i++) 
      sum = sum + values[i] * magicValues[i]; 
 
     int dividend = sum / 11; 
     int product = dividend * 11; 
     int substraction = sum - product; 
     checkDigit = (substraction > 0) ? 11 - substraction : substraction; 
 
     res = (checkDigit == values[i]); 
    } 
 
   } catch (Exception e) { 
    e.printStackTrace(); 
   } 
  } 
  if(!res) 
	  System.out.println("El CUIT es invalido");
 } 
}
