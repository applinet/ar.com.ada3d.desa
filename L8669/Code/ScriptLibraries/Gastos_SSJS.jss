/**
 * @return : frmGastos.xsp si tiene una configuracion o frmOpcionesGastos.xsp si debe cargar una.
 */

function validarConfiguracionGasto():String{
	edificioBean.actualizoUnEdificioAs400(sessionScope.edfObj, "");
	if(sessionScope.edfObj.edf_ConfigNumeroProximoGasto != null){ 
		gastoBean.createNewGasto();
		return("frmGastos.xsp");
	}else{//no tiene la configuracion cargada ver si tiene ***
		if(viewScope.scopeOpcionGastoMaestro == null)
			viewScope.put("scopeOpcionGastoMaestro", gastoBean.getOpcionGastoMaestro());
			
		if(viewScope.scopeOpcionGastoMaestro == null){//No tengo ***
			sessionScope.put("navRule", "viewGastos");
			gastoOpcionesBean.createNewOpcionGasto();
			gastoOpcionesBean.gastoOpciones.setMensajeWarning("Puede elegir entre tener una configuración individual para cada edificio o una única configuración para todos sus edificios. Si tilda la opción:'*** Configuración única para todos mis edificios', todos sus edificios compartirán esta configuración y incluso cuando se incorpore un nuevo edificio.");
			return("frmOpcionesGastos.xsp");
		}else{//Tengo maestro(***) y no tengo edificio
			if (viewScope.scopeOpcionGastoMaestro.getTipoNumeracion().equals("0") || viewScope.scopeOpcionGastoMaestro.getTipoNumeracion().equals("3")){
				sessionScope.edfObj.setEdf_ConfigOrdenDetalleGasto(viewScope.scopeOpcionGastoMaestro.getOrdenDatosProveedorEnDetalleDelGasto());
				gastoBean.createNewGasto();
				return("frmGastos.xsp");
			}else{//Tengo que cargar el nro automático
				sessionScope.put("navRule", "viewGastos");
				gastoOpcionesBean.createNewOpcionGasto(viewScope.scopeOpcionGastoMaestro);
				gastoOpcionesBean.gastoOpciones.setMensajeWarning("Por favor complete el campo 'Numerador de comprobantes' del edificio para poder crear un gasto. Esto es necesario debido a que ha optado por numeración automática.");
				return("frmOpcionesGastos.xsp");
			}
		}
	}
}