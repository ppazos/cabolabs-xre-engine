package com.cabolabs.xre.engine.resolution.predefined

import com.cabolabs.xre.core.definitions.VariableDeclaration
import com.cabolabs.xre.core.execution.VariableInstance
import com.cabolabs.xre.core.resolution.VariableResolution
import com.cabolabs.xre.core.definitions.DataType
import com.cabolabs.xre.engine.logic.dates.Now
import com.cabolabs.xre.engine.logic.dates.Today
import com.cabolabs.xre.engine.logic.dates.Tomorrow
import com.cabolabs.xre.engine.logic.dates.Yesterday
import com.cabolabs.xre.engine.resolution.function.FunctionVariableResolution

/**
 * Las resolutions se necesitan para obtener los valores de
 * las variables predefinidas durante la inicializacion de
 * cada ejecucion de regla.
 * 
 * @author Pablo Pazos Gutierrez
 *
 */
class PredefinedResolutions {

   static VariableResolution now(VariableDeclaration declaration)
   {
      VariableResolution vr = new FunctionVariableResolution(declaration)
      
      // No es necesario setearle el returName porque FunctionVariableResolution devuelve
      // con el nombre de la declaracion de la resolution aunque la funcion tenga o no
      // returnName (porque es la variable que se esta resolviendo con la funcion).
      //vr.setFunction( new Now("\$now", DataType.DATE) )
      vr.setFunction( new Now(null, DataType.DATE) )
      return vr
   }
   
   static VariableResolution today(VariableDeclaration declaration)
   {
      VariableResolution vr = new FunctionVariableResolution(declaration)
      vr.setFunction( new Today(null, DataType.DATE) )
      return vr
   }
   
   static VariableResolution yesterday(VariableDeclaration declaration)
   {
      VariableResolution vr = new FunctionVariableResolution(declaration)
      vr.setFunction( new Yesterday(null, DataType.DATE) )
      return vr
   }
   
   static VariableResolution tomorrow(VariableDeclaration declaration)
   {
      VariableResolution vr = new FunctionVariableResolution(declaration)
      vr.setFunction( new Tomorrow(null, DataType.DATE) )
      return vr
   }
   
   // TODO:
   // Para implementar ruleId se necesita el RuleExecution
   // Puedo pasarle un context como parametro a todas las funciones
   // y dentro del context que este el RuleExecution.
}