/*Esta libreria maneja el menu que vemos en la pagina de administraciones */
var menuTools = {
    "getMenu" : function(nsf:NotesDatabase, menuName:string) {
        /* Chequear primero si es session o applicationScope*/
        dBar.info("llego aca?");
        if (!sessionScope.menuList) {
            sessionScope.put("menuList", new java.util.HashMap());
        }
        if (sessionScope.menuList.containsKey(menuName)) {
            return sessionScope.menuList.get(menuName);
        }
        /* Devuelve las entradas de menú basandose en una vista que toma de configuración*/
        var viewClave:NotesView = nsf.getView(getOpcionesClave("VIEW_MODULOS", "codigo")[0]); //vista 'vModulosTreeview'
        var viewNav:NotesViewNavigator = viewClave.createViewNav();
        var viewEntry = viewNav.getFirst();
        var result = [];
        var menuSelected:java.util.Vector = getCodigoMenuSelected();
        if (menuSelected.isEmpty()) {
        	if(userBean.accessRoles.toString().contains('[usrInitial]')){
        		/*Para pasar solo una vez en el lifecycle*/
        		if (requestScope.containsKey("msg"))
        			return;
        		requestScope.put("msg", "si");
	        	addFacesMessage("El usuario actual es temporal. Ahora deberá crear su usuario de seguridad.", null, "WARNING")
	        	addFacesMessage("Una vez creado el nuevo usuario, le solicitará ingresar con el nuevo usuario.", null, "WARNING")
        	}else{
	        	addFacesMessage("No se pudo cargar el menú, comuniquese con el usuario de seguridad.", null, "FATAL")
        	}
        	return;
        }
        var strMenuAlias:String = "";
             
        while (viewEntry != null) {
        	/* Revisa los padres de la vista 'vModulosTreeview'*/
            var nextviewEntry = viewNav.getNextSibling(viewEntry);
            var lineResult = {};
            strMenuAlias = viewEntry.getColumnValues()[1];
            
            if (menuSelected.contains(strMenuAlias.split('|')[2]) || isUSRSEG()) { /*Esto hace lo mismo que explico mas abajo estos son padres */
	            lineResult.name = strMenuAlias.split('|')[0];
		        lineResult.items = [];
		        lineResult.page = [];
		        
		        //Solo va a cargar los hijos si el padre es autorizado
	            var child = viewNav.getChild(viewEntry);
	            while (child != null) {
	            	/* Revisa los hijos de la vista 'vModulosTreeview'*/
	            	 
	                var nextChild = viewNav.getNextSibling(child);
	                
	                /*  Cargo un atring con los datos de la vista
	                 *  La columna de la vista devuelve = Descripcion | pagina | codigo unico de modulo
	                 *  Al string lo divido en un array de 3 posiciones separando por |
	                 *  menuSelected lo carge arriba con los codigos que tiene el usuario seleccionados
	                 *  Comparo menuSelected si contiene array[2] que es el codigo, si es el mismo cargo el menu
	                 *  O tamabien cargo el menu si es usuario de seguridad   
	                 * */
	                strMenuAlias = child.getColumnValues()[1];
	                if (menuSelected.contains(strMenuAlias.split('|')[2]) || isUSRSEG()) { /* [2]= codigo que comparo con menuSelected del usuario */
	                	
		                lineResult.items.push(strMenuAlias.split('|')[0]); /* A items le cargo la descricion que voy a ver en el menu*/
		                lineResult.page.push(strMenuAlias.split('|')[1]);  /* A page le cargo la pagina linkeada que tiene este menu*/
	                }
	                child.recycle();
	                child = nextChild;
	            }
	            result.push(lineResult);
            } //End si el menu está en los autorizados
	        
            viewEntry.recycle();
            viewEntry = nextviewEntry;
        }
 
        try {
            viewNav.recycle();
            viewClave.recycle();
        } catch (e) {
            print(e);
        }
        // Se guarda en cache
        sessionScope.menuList.put(menuName, result);
        return result;
    }   
}

/*Devuelve un java vector con los menues autorizados del usuario actual logueado*/
function getCodigoMenuSelected():java.util.Vector{
	var docUsuario:NotesDocument = getUserDocument();
	if (docUsuario != null){
		return docUsuario.getItemValue("usr_MenuSelected_cod");
	}
	return new java.util.Vector // Devuelvo un Vector vacio para despues validar
}	