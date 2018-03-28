package ar.com.ada3d.frontend;

import com.ibm.xsp.extlib.tree.impl.BasicLeafTreeNode;
import com.ibm.xsp.extlib.tree.impl.BasicNodeList;

public class LayoutTitleBarActions extends BasicNodeList {
private static final long serialVersionUID = 1L;

public LayoutTitleBarActions() {
	addLeaf("Nuevo Gasto", "btnNuevoGasto");
	addLeaf("Alta de Proveedor", "btnSave");
	addLeaf("Pagar", "btnPagar");
	addLeaf("Recalcular", "btnClose");
	
}

private void addLeaf(String label, String boton) {
	
	BasicLeafTreeNode node = new BasicLeafTreeNode();
	node.setLabel(label);
	String script = "dojo.query(\"[id$='" + boton + "']\")[0].click();";
    node.setOnClick(script);            
	addChild(node);
}

}