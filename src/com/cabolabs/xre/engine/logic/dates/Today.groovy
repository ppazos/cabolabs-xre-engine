package com.cabolabs.xre.engine.logic.dates

import com.cabolabs.xre.core.definitions.DataType
import com.cabolabs.xre.core.execution.RuleExecution
import com.cabolabs.xre.core.execution.VariableInstance
import com.cabolabs.xre.core.logic.Function
import com.cabolabs.xre.core.execution.Memory

class Today extends Function {

   public Today(String returnName, DataType returnType)
   {
      super(returnName, returnType)
   }
   
   @Override
   public List<VariableInstance> execute(String sessionId)
   {
      return this.functionReturnValue(new Date().clearTime(), Memory.getInstance().get(sessionId))
   }
}