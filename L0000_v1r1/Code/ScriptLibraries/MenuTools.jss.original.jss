var menuTools = {
         
    "getMenu" : function(nsf:NotesDatabase, menuName:string) {
        /* Chequear primero si es session o applicationScope*/
        if (!sessionScope.menuList) {
            sessionScope.put("menuList", new java.util.HashMap());
        }
        if (sessionScope.menuList.containsKey(menuName)) {
            return sessionScope.menuList.get(menuName);
        }
 
        /* Devuelve las entradas de menú basandose en una vista */  
        var viewClave:NotesView = nsf.getView("clave");
        var viewNav:NotesViewNavigator = viewClave.createViewNav();
        var viewEntry = viewNav.getFirst();
        var result = [];
             
        while (viewEntry != null) {
            var nextviewEntry = viewNav.getNextSibling(viewEntry);
            var lineResult = {};
            lineResult.name = viewEntry.getColumnValues()[0];
         
	        lineResult.items = [];      
            var child = viewNav.getChild(viewEntry);
            while (child != null) {
                var nextChild = viewNav.getNextSibling(child);
                lineResult.items.push(child.getColumnValues()[1]);
                child.recycle();
                child = nextChild;
            }
            result.push(lineResult);
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