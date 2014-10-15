package com.cabolabs.xre.engine.resolution.predefined

import com.cabolabs.xre.core.definitions.VariableDeclaration
import com.cabolabs.xre.core.execution.VariableInstance
import com.cabolabs.xre.core.definitions.DataType

/**
 * Declaraciones de las variables predefinidas
 * @author Pablo Pazos Gutierrez
 *
 */
class PredefinedDeclarations {

   static VariableDeclaration now()
   {
      return new VariableDeclaration("\$now", DataType.DATE)
   }
   
   static VariableDeclaration today()
   {
      return new VariableDeclaration("\$today", DataType.DATE)
   }
   
   static VariableDeclaration yesterday()
   {
      return new VariableDeclaration("\$yesterday", DataType.DATE)
   }
   
   static VariableDeclaration tomorrow()
   {
      return new VariableDeclaration("\$tomorrow", DataType.DATE)
   }
   
   // TODO:
   // Para implementar ruleId se necesita el RuleExecution
   // Puedo pasarle un context como parametro a todas las funciones
   // y dentro del context que este el RuleExecution.
}