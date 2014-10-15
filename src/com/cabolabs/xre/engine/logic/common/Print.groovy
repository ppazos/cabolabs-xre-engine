package com.cabolabs.xre.engine.logic.common;

import com.cabolabs.xre.core.definitions.DataType
import com.cabolabs.xre.core.execution.Memory
import com.cabolabs.xre.core.execution.RuleExecution
import com.cabolabs.xre.core.execution.VariableInstance
import com.cabolabs.xre.core.logic.Function

public class Print extends Function {

   public Print(String returnName, DataType returnType)
   {
      super(returnName, returnType);
   }

   @Override
   public List<VariableInstance> execute(String sessionId)
   {
      RuleExecution re = Memory.getInstance().get(sessionId)

      //VariableInstance par1 = re.getValue(params.get(0))
      // La accion es print
      //println par1.getValue()
      
      // Puede aceptar mas de un parametro y se imprimen concatenados
      params.each {
         print re.getValue(it).getValue()
      }
      println ""
      
      
      // No devuelve nada
      return null
   }
}