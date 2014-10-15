package logic.actions

import rules.Cloner
import values.Variable

class Sum extends Action {

   /**
    * Valores a sumar, la setea alguien desde afuera.
    */
   Variable v1
   Variable v2
   
   
   @Override
   public Object execute()
   {
      def returnValue = v1.value + v2.value
      
      //println "Sum " + v1.name + ' ' + v2.name + ' = ' + returnValue
      
      // returnName es atributo de Action
      // TODO: considerar el type, sino pone type=string 
      def ret = new Variable(name:this.returnName, value:returnValue)
      return ret
   }

   @Override
   public Action clone(Cloner cloner)
   {
      return new Sum(
        returnName: this.returnName,
        v1: cloner.clone(this.v1),
        v2: cloner.clone(this.v2),
        type: this.type
      )
   }
}