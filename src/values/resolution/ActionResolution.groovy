package values.resolution

import logic.actions.Action
//import parser.Caster

// FIXME: deberia haber una interfaz IResolution con getType y getValue
//        que esta clase implemente.
class ActionResolution {

   Action action
   String type = "action"

   public Object getValue()
   {
      // TODO: ejecucion de la accion para obtener el resultado
      // action.execute devuelve Variable y hay que devolver un tipo simple para que Rule.init asigne a la variable correcta
      def varb = action.execute()
      
      //println "ActionResolution var: " + varb.name + " " + varb.value
      
      return varb.value
   }
}