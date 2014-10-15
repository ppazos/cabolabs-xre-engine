package values.predefined

import values.Variable
import values.ValueType

class Now extends Variable {

   public Now()
   {
      this.name = "\$now"
      this.type = ValueType.DATE
   }
   
   @Override
   def getValue()
   {
      //println " --- NOW getValue ---"
      return new Date()
   }
}