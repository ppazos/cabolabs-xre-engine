package com.cabolabs.xre.engine.logic.bool;

import com.cabolabs.xre.core.definitions.DataType
import com.cabolabs.xre.core.execution.Memory
import com.cabolabs.xre.core.execution.RuleExecution
import com.cabolabs.xre.core.execution.VariableInstance
import com.cabolabs.xre.core.logic.bool.BooleanFunction

public class GreaterThan extends BooleanFunction {

   public GreaterThan(String returnName, DataType returnType)
   {
      super(returnName, returnType)
   }

   @Override
   public List<VariableInstance> execute(String sessionId)
   {
      // Para obtener los valores de los parametros desde la memoria
      RuleExecution re = Memory.getInstance().get(sessionId)
      
      // El RuleExecuter deberia setearle los params luego de ejecutar las resolutions.
      VariableInstance par1 = re.getValue(params.get(0))
      VariableInstance par2 = re.getValue(params.get(1))
      
      println (par1.getValue() +" > "+ par2.getValue())
      
      // Todo lo anterior en una sola linea
      return this.functionReturnValue(par1.getValue() > par2.getValue(), re)
   }
}