package parser.actions

import java.util.Map

// A veces viene NodeChild pero a veces manda NodeChildren con un elemento
// La primer superclase de ambase es GPathResult
//import groovy.util.slurpersupport.NodeChild
import groovy.util.slurpersupport.GPathResult

import logic.actions.Action
import logic.actions.Sum
import values.Variable

class SumParser extends ActionParser {

   @Override
   public Action parse(GPathResult node, Map variables, Map params)
   {
      def name1 = node.@in1.text()
      def cte1 = node.@inc1.text()
      def value1
      if (variables.containsKey(name1))
      {
         value1 = variables[name1]
      }
      else if (params.containsKey(name1))
      {
         value1 = params[name1]
      }
      else if (cte1) // Si es una constante
      {
         // Si hay valor, es una constante sin nombre
         value1 = new Variable(value: cte1)
      }
      
      
      def name2 = node.@in2.text()
      def cte2 = node.@inc2.text()
      def value2
      if (variables.containsKey(name2))
      {
         value2 = variables[name2]
      }
      else if (params.containsKey(name2))
      {
         value2 = params[name2]
      }
      else if (cte2) // Si es una constante
      {
         // Si hay valor, es una constante sin nombre
         value2 = new Variable(value: cte2)
      }
      
      return new Sum( v1: value1, v2: value2,
                      returnName: node.@return.text(),
                      type: node.@type.text())
   }

}