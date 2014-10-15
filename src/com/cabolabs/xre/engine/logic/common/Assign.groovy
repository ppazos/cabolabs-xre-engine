package com.cabolabs.xre.engine.logic.common;

import com.cabolabs.xre.core.definitions.DataType
import com.cabolabs.xre.core.execution.Memory
import com.cabolabs.xre.core.execution.RuleExecution
import com.cabolabs.xre.core.execution.VariableInstance
import com.cabolabs.xre.core.logic.Function

public class Assign extends Function {

   public Assign(String returnName, DataType returnType)
   {
      super(returnName, returnType);
   }

   @Override
   public List<VariableInstance> execute(String sessionId)
   {
      // Para obtener los valores de los parametros desde la memoria
      RuleExecution re = Memory.getInstance().get(sessionId)
      
      // El RuleExecuter deberia setearle los params luego de ejecutar las resolutions.
      VariableInstance par1 = re.getValue(params.get(0))
      
      println (this.returnName +" <- "+ par1.getValue())
      
      // Devuelve el valor del parametro con el nombre returnName
      // TODO: verificar asignacion entre tipos incompatibles, ej. si el
      //       returnName ya fue declarado en la regla pero con un tipo
      //       incompabile con el de par1.
      return this.functionReturnValue(par1.getValue(), re)
   }
}