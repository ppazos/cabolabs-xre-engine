package rules

import values.Variable

/**
 * Contexto de ejecucion de una unidad, donde se registran las variables a medida que se declaran y se calculan sus valores.
 * @author pab
 *
 */
class Context {

   Map values = [:]
   
   // identificador seteado por executer para identificar la regla que se esta ejecutando
   // se genera en el init y se usa para hacer el execute
   String executionSessionId
   
   
   public Context(String sessionId)
   {
      this.executionSessionId = sessionId
   }
   
   public String getSessionId()
   {
      return this.executionSessionId
   }
   
   public void addValue(Variable val)
   {
      values[val.name] = val
   }
   
   /**
    * @param Map values nombre->NamedValue
    */
   public void addAllValues(Map _values)
   {
      values.putAll(_values)
   }
   
   public Variable getValue(String name)
   {
      return values[name]
   }
   
   @Override
   public String toString()
   {
      return 'Context '+ values.toString()
   }
}