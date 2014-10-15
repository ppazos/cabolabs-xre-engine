package logic.actions

import rules.Cloner
import rules.Context

abstract class Action {

   /**
    * Las acciones que retornan un valor
    * especifican un nombre para ese valor.
    */
   String returnName
   String type // action name e.g. print, sum // FIXME: los parsers de acciones no estan usando este campo con este proposito (ver DateDiffParser)
   
   Context context // repositorio de varaibles y parametros necesarios para la ejecucion de reglas y condiciones
   
   abstract public Object execute()
   
   // Es necesario pasarle el cloner porque hay estado (referencias a variables declaradas que debe usar)
   // FIXME: estas referencias no pueden ser NamedValueRef y tener todas las variables declaradas accesibles desde los refs?
   abstract public Action clone(Cloner cloner)
   
   /**
   * Necesario para implementar el clone
   */
   boolean equals(Object other)
   {
      other instanceof Action && this.type == other.type
   }
}