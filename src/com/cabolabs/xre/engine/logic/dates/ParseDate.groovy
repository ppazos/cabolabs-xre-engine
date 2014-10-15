package com.cabolabs.xre.engine.logic.dates

import java.util.List;

import com.cabolabs.xre.core.definitions.DataType
import com.cabolabs.xre.core.execution.RuleExecution
import com.cabolabs.xre.core.execution.VariableInstance
import com.cabolabs.xre.core.logic.Function
import com.cabolabs.xre.core.execution.Memory
import com.cabolabs.xre.engine.util.DateDifference;

class ParseDate extends Function {

   public ParseDate(String returnName, DataType returnType)
   {
      super(returnName, returnType)
   }
   
   
   @Override
   public List<VariableInstance> execute(String sessionId)
   {
      println "ParseDate execute() "+ params.name
      
      // Para obtener los valores de los parametros desde la memoria
      RuleExecution re = Memory.getInstance().get(sessionId)
      
      println "rule execution id = " + re.getId()
      println re.getValues()
      
      // El RuleExecuter deberia setearle los params luego de ejecutar las resolutions.
      VariableInstance stringDate = re.getValue(params.get(0))
      
      // Parsea buscando el formato
      Date returnValue = com.cabolabs.xre.engine.util.DateFormats.parse(stringDate.getValue())
      
      return this.functionReturnValue(returnValue, re)
      
   }
}