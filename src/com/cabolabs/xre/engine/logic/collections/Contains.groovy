package com.cabolabs.xre.engine.logic.collections;

import com.cabolabs.xre.core.definitions.DataType
import com.cabolabs.xre.core.execution.Memory
import com.cabolabs.xre.core.execution.RuleExecution
import com.cabolabs.xre.core.execution.VariableInstance
import com.cabolabs.xre.core.logic.bool.BooleanFunction

public class Contains extends BooleanFunction {

   public Contains(String returnName, DataType returnType)
   {
      super(returnName, returnType);
   }

   @Override
   public List<VariableInstance> execute(String sessionId)
   {
      // Para obtener los valores de los parametros desde la memoria
      RuleExecution re = Memory.getInstance().get(sessionId)
      
      // El RuleExecuter deberia setearle los params luego de ejecutar las resolutions.
      VariableInstance par1 = re.getValue(params.get(0)) // collection
      VariableInstance par2 = re.getValue(params.get(1)) // object
      
      println par1.getValue().toString() +" contains? "+ par2.getValue().toString()
      
      // Todo lo anterior en una sola linea
      return this.functionReturnValue(((Collection)par1.getValue()).contains( par2.getValue() ), re)
   }
}