package logic.actions

import rules.Cloner
import values.NamedValueRef
import values.Variable

class Print extends Action {

   /**
    * Valor a mostrar, la setea alguien desde afuera.
    * FIXME: no se puede setear desde afuera si es un valor que se va
    *  a calcular en el futuro, ej. resultado de una accion, deberia
    *  guardarse una referencia para uso posterior: en el momento de
    *  hacer el print debe estar declarado y resuelto el valor.
    *  Tengo el valor solo si es una constante.
    */
   
   // TODO: es uno u el otro, no puede tener los 2 (sino la ejecucion funciona mal)
   
   // Tengo el valor si es una constante
   // FIXME: si es una constante, siempre es un String...
   Variable value
   
   // Tengo una referencia al valor si es una variable
   NamedValueRef valueRef
   
   
   @Override
   public Object execute()
   {
      if (value)
         println value.value
      else
         println this.context.getValue(valueRef.name).value
         
   }

   @Override
   public Action clone(Cloner cloner)
   {
      def act = new Print(
         //returnName: this.returnName, // esta no tiene returnName porque no tiene return
         value: (!this.value ? null : cloner.clone(this.value)), // sino tiene value, no clono
         valueRef: (!this.valueRef ? null : cloner.clone(this.valueRef))) // sino tiene ref no clono
      return act
   }
}