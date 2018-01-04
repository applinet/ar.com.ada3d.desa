package ar.com.ada3d.utilidades;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Conversores {

	
	/**
	 * - Funcion StringToDate - Parseo un string y saco un JavaDate.
	 * Este formato sirve para un NotesDateTime
	 * 
	 * @param inputFormat
	 *            : String que voy a transformar
	 * @param inputTimeStamp
	 *            : Formato del String que ingresa(inputFormat). ej:"yyyyMMdd"
	 */
	public static Date StringToDate(final String inputFormat, String inputTimeStamp) {
		
		SimpleDateFormat formatter = new SimpleDateFormat(inputFormat);
        Date convertedDate = null;
		try {
			convertedDate = (Date) formatter.parse(inputTimeStamp);
		} catch (ParseException e) {
			e.printStackTrace();
		}
        return convertedDate;


	}
	
	/**
	 * - Funcion StringDateToStringDate - Parseo un string con formato de ingreso y egreso.
	 * Este formato sirve para un NotesDateTime
	 * 
	 * @Ejemplo parseo: new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new SimpleDateFormat("yyyyMMdd").parse("20140625")).toString();
	 * 
	 * @param inputFormat
	 *            : String que voy a transformar
	 * @param inputTimeStamp
	 *            : Formato del String que ingresa(inputFormat). ej:"yyyyMMdd"
	 * @param outputFormat
	 *            : El formato que va a tener el dt que devuelvo ej:"dd/MM/yyyy hh:mm"
	 */
	public static String StringDateToStringDate(final String inputFormat, String inputTimeStamp,
			final String outputFormat) {
		
			String fecha = null;
			try {
				fecha = new SimpleDateFormat(outputFormat).format(new SimpleDateFormat(inputTimeStamp).parse(inputFormat))
						.toString();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		return fecha;
	}
	
}

