function getDbByKey (strKey:String):NotesDatabase {
	
	var viewCfg:NotesView = session.getCurrentDatabase().getView("v.Sys.Cfg");

	var docCfg:NotesDocument = viewCfg.getDocumentByKey(strKey, true)
	var strServer:String = docCfg.getItemValueString("conf_server");
	var strPath:String = docCfg.getItemValueString("conf_path");

	return session.getDatabase(strServer, strPath, false);
}