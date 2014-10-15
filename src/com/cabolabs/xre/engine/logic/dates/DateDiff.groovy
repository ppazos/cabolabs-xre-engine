package com.cabolabs.xre.engine.logic.dates

import com.cabolabs.xre.core.definitions.DataType
import com.cabolabs.xre.core.execution.RuleExecution
import com.cabolabs.xre.core.execution.VariableInstance
import com.cabolabs.xre.core.logic.Function
import com.cabolabs.xre.core.execution.Memory
import com.cabolabs.xre.engine.util.DateDifference;

class DateDiff extends Function {

   public DateDiff(String returnName, DataType returnType)
   {
      super(returnName, returnType)
   }
   
   
   @Override
   public List<VariableInstance> execute(String sessionId)
   {
      //println "DateDiff execute() "+ params
      

      // Para obtener los valores de los parametros desde la memoria
      RuleExecution re = Memory.getInstance().get(sessionId)
      
      //println "rule execution id = " + re.getId()
      //println re.getValues()
      
      // El RuleExecuter deberia setearle los params luego de ejecutar las resolutions.
      VariableInstance date1 = re.getValue(params.get(0))
      VariableInstance date2 = re.getValue(params.get(1))
      VariableInstance units = re.getValue(params.get(2))
      
      //println (date1.getValue() +" dateDiff "+ date2.getValue())
      
      
      Integer returnValue = -1
      
      switch (units.value)
      {
         case "years":
            returnValue = DateDifference.numberOfYears(date1.value, date2.value)
         break
         case "months":
            returnValue = DateDifference.numberOfMonths(date1.value, date2.value)
         break
         case "days":
            returnValue = DateDifference.numberOfDays(date1.value, date2.value)
         break
         case "hours":
            returnValue = DateDifference.numberOfHours(date1.value, date2.value)
         break
         case "minutes":
            returnValue = DateDifference.numberOfMinutes(date1.value, date2.value)
         break
         case "seconds":
            returnValue = DateDifference.numberOfSeconds(date1.value, date2.value)
         break
         default:
           throw new Exception("units not supported '"+ units.value +"'")
      }
      
      return this.functionReturnValue(returnValue, re)
   }
}