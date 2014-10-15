package com.cabolabs.xre.engine.logic.bool;

import com.cabolabs.xre.core.definitions.DataType
import com.cabolabs.xre.core.execution.Memory
import com.cabolabs.xre.core.execution.RuleExecution
import com.cabolabs.xre.core.execution.VariableInstance
import com.cabolabs.xre.core.logic.bool.BooleanFunction

public class False extends BooleanFunction {

   public False(String returnName, DataType returnType)
   {
      super(returnName, returnType)
   }

   @Override
   public List<VariableInstance> execute(String sessionId)
   {
      // Para obtener los valores de los parametros desde la memoria
      RuleExecution re = Memory.getInstance().get(sessionId)
      
      return this.functionReturnValue( false, re )
   }
}