package parser.actions

// A veces viene NodeChild pero a veces manda NodeChildren con un elemento
// La primer superclase de ambase es GPathResult
//import groovy.util.slurpersupport.NodeChild
import groovy.util.slurpersupport.GPathResult

import logic.actions.Action
import logic.actions.Print
import values.NamedValueRef
import values.Variable

class PrintParser extends ActionParser {

   // FIXME: es horrible que le tenga que pasar las variables y params declarados
   // en el xrl, deberian estar accesibles de forma global durante el parsing
   // o hacer un IOC para configurar este parser con esos valores
   @Override
   public Action parse(GPathResult node, Map variables, Map params)
   {
      if (node.@in1.isEmpty() && node.@inc1.isEmpty())
         throw new Exception('la accion print necesita un valor para mostrar, debe declarar el atributo in1 o inc1 en la accion')
      
      def name = node.@in1.text()
      def cte = node.@inc1.text()
      
      if (!name && !cte)
         throw new Exception('La accion println necesita un valor o una referencia')
      
      def value
      if (variables.containsKey(name))
      {
         value = variables[name]
      }
      else if (params.containsKey(name))
      {
         value = params[name]
      }
      else if (cte) // Si es una constante
      {
         // Si hay valor, es una constante sin nombre
         value = new Variable(value: cte)
      }
      
      if (value)
      {
         return new Print( value: value )
      }
      else // es una referencia por nombre (cae aca cuando hace print de un valor de retorno)
      {
         value = new NamedValueRef(name: name)
         return new Print( valueRef: value )
      }
   }
}