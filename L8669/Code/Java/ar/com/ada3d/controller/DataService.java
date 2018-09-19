package ar.com.ada3d.controller;

import javax.servlet.http.HttpServletResponse;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Session;
import lotus.domino.View;
import org.json.JSONArray;
import org.json.JSONObject;
import com.ibm.domino.services.ServiceException;
import com.ibm.domino.services.rest.RestServiceEngine;
import com.ibm.xsp.extlib.component.rest.CustomService;
import com.ibm.xsp.extlib.component.rest.CustomServiceBean;
import com.ibm.xsp.extlib.util.ExtLibUtil;
 
public class DataService extends CustomServiceBean {
 
@Override
public void renderService(CustomService service, RestServiceEngine engine) throws ServiceException {
    try {
        //Create an empty JSONArray. This array will contain all data objects.
        JSONArray jsonResponse = new JSONArray();
 
        //Create a Domino session
        Session session = ExtLibUtil.getCurrentSessionAsSignerWithFullAccess();
        Database thisdb = session.getCurrentDatabase();
        View view = thisdb.getView("v.Sys.CorrHab");
 
        //Loop through all documents found in the view
        Document doc = view.getFirstDocument();
        while (doc != null) {
            //TODO: You should do some checks here... is value empty, etc...
 
            //Create a separate JSON Object for each document
            JSONObject jsonObject = new JSONObject();
 
            //Add properties to the JSONObject, you want to return
            jsonObject.put("status", doc.getItemValueString("Habilitar_Corr"));
            jsonObject.put("TipoDatoGuardado_Corr", doc.getItemValueString("TipoDatoGuardado_Corr"));
            jsonObject.put("Form_Corr", doc.getItemValueString("Form_Corr"));
            jsonObject.put("universalID", doc.getUniversalID());
 
            //Add the JSONObject to the JSONArray
            jsonResponse.put(jsonObject);
 
            //process next document in view
            doc = view.getNextDocument(doc);
        }
        //Create a response
        HttpServletResponse response = engine.getHttpResponse();
 
        //Change response content type to JSON
        response.setHeader("Content-Type", "application/json; charset=UTF-8"); 
 
        //Send the created JSONObject as response
        response.getWriter().write(jsonResponse.toString());
        response.getWriter().close();
    } catch (Exception e) {
        e.printStackTrace();
        }
    }
}
