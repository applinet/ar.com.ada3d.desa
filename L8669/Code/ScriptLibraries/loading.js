dojo.require('dijit.Dialog')

function loading() {
   underlay = new dijit.DialogUnderlay({'class': 'loading'});
   underlay.show();
}
function stoploading(){
   underlay.hide()
   }
