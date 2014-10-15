package logic.actions

import java.util.Date;

import rules.Cloner
import values.Variable
import values.ValueType
import util.DateDifference

class DateDiff extends Action {

   /**
    * Valores dates, la setea alguien desde afuera.
    */
   Variable v1
   Variable v2
   String units // years, months, days, hours, minutes, seconds
   
   
   @Override
   public Object execute()
   {
      def returnValue // = v1.value + v2.value
      
      //println "DateDiff: " + v1.value + ", " + v2.value
      
      switch (units)
      {
         case "years":
            returnValue = DateDifference.numberOfYears(v1.value, v2.value)
         break
         case "months":
            returnValue = DateDifference.numberOfMonths(v1.value, v2.value)
         break
         case "days":
            returnValue = DateDifference.numberOfDays(v1.value, v2.value)
         break
         case "hours":
            returnValue = DateDifference.numberOfHours(v1.value, v2.value)
         break
         case "minutes":
            returnValue = DateDifference.numberOfMinutes(v1.value, v2.value)
         break
         case "seconds":
            returnValue = DateDifference.numberOfSeconds(v1.value, v2.value)
         break
         default:
           throw new Exception("units not supported '$units'")
      }
      
      
      //println "DateDiff: " + v1.name + ' ' + v2.name + ' = ' + returnValue

      return new Variable(name:this.returnName, value:returnValue, type:ValueType.INTEGER)
   }

   @Override
   public Action clone(Cloner cloner)
   {
      return new DateDiff(
        returnName: this.returnName,
        v1: cloner.clone(this.v1),
        v2: cloner.clone(this.v2),
        units: this.units,
        type: type
      )
   }
}