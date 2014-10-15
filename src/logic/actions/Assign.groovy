package logic.actions

import rules.Cloner
import values.ValueType
import values.Variable

class Assign extends Action {

   /**
    * El nombre de la variable nueva se pone en this.returnName.
    */
   
   /**
    * Valor a asignar.
    */
   Variable value
   
   /**
    * Tipo de la varaible a retornar.
    */
   ValueType returnType = ValueType.STRING // Por defecto es String igual que con las variables
   
   
   @Override
   public Object execute()
   {
      println "Assign.execute"
      
      def returnValue = value.value
      
      // Si hay una variable en contexto con el returnValue, asignar el valor en esa variable
      // Sino, retornar una nueva variable
      def var = this.context.getValue(this.returnName)
      if (var)
      {
         // Verifica que el tipo de la variable a la que se asigna el valor,
         // sea del mismo tipo que la variable que se va a retornar
         if (var.type != this.returnType)
         {
            throw new Exception("Variable type is different from the variable to assign: " + var.type +" vs. "+ this.returnType)
         }
         
         var.value = returnValue
      }
      else
      {
         var = new Variable(name:this.returnName, value:returnValue, type:this.returnType)
      }
      
      println "Assign " + value.name + ' to ' + this.returnName
      
      return var
   }

   @Override
   public Action clone(Cloner cloner)
   {
      return new Assign(
        returnName: this.returnName,
        value: cloner.clone(this.value),
        returnType: this.returnType,
        type: this.type
      )
   }
}