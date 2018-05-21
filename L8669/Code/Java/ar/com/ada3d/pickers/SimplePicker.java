package ar.com.ada3d.pickers;

import java.util.ArrayList;
import java.util.List;
import ar.com.ada3d.controller.ProveedorBean;
import com.ibm.xsp.extlib.component.picker.data.IPickerEntry;
import com.ibm.xsp.extlib.component.picker.data.IPickerOptions;
import com.ibm.xsp.extlib.component.picker.data.IPickerResult;
import com.ibm.xsp.extlib.component.picker.data.IValuePickerData;
import com.ibm.xsp.extlib.component.picker.data.SimplePickerResult;


public class SimplePicker implements IValuePickerData {
	  
	public SimplePicker() {
	}

	public IPickerResult readEntries(IPickerOptions options) {
		String startKey = options.getStartKey();
		List<IPickerEntry> entries = new ArrayList<IPickerEntry>();
		ProveedorBean prvBean = new ProveedorBean();
		
		ArrayList<String> listaProveedores = prvBean.getArraySrtringListaProveedores(false);
		String valor;
		String alias; 
		for (int i = 0; i < listaProveedores.size(); i++) {
			valor = listaProveedores.get(i).replaceAll("\\|", " - ");
			alias = listaProveedores.get(i).split("\\|")[0].trim() + " (" + listaProveedores.get(i).split("\\|")[2].trim() + ")";
			if (startKey != null){
				if(listaProveedores.get(i).toLowerCase().contains(startKey.toLowerCase()))
					entries.add(new SimplePickerResult.Entry(alias, valor));			
			}else{
				entries.add(new SimplePickerResult.Entry(alias, valor));			
				
			}
		}
		return new SimplePickerResult(entries, -1);
	}
	 
	
	
	  public String[] getSourceLabels() {
	    return null;
	  }
	   
	  public boolean hasCapability(int capability) {
	    return false;
	  }
	   
	  public List<IPickerEntry> loadEntries(Object[] ids, String[] attributeNames) {
	      return null;
	  }
}
