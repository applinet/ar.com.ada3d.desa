package ar.com.ada3d.frontend;

import com.ibm.xsp.extlib.tree.impl.BasicLeafTreeNode;
import com.ibm.xsp.extlib.tree.impl.BasicNodeList;

public class LayoutTitleBarActions extends BasicNodeList {
private static final long serialVersionUID = 1L;


	public LayoutTitleBarActions() {
		addLeaf("Nuevo Gasto", "btnNuevoGasto", "btn-info");
		addLeaf("Alta de Proveedor", "btnNewProveedor", "btn-info");
		addLeaf("Pagar", "btnPagar", "");
		addLeaf("Recalcular", "btnClose", "");
		
	}
	
	public void addLeaf(String label, String boton, String styleClass) {
		
		BasicLeafTreeNode node = new BasicLeafTreeNode();
		node.setLabel(label);
		String script = "dojo.query(\"[id$='" + boton + "']\")[0].click();";
	    node.setOnClick(script);
	    node.setStyleClass(styleClass);
		addChild(node);
	}

}
