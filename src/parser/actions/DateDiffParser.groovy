package parser.actions

// A veces viene NodeChild pero a veces manda NodeChildren con un elemento
// La primer superclase de ambase es GPathResult
//import groovy.util.slurpersupport.NodeChild
import groovy.util.slurpersupport.GPathResult
import logic.actions.Action
import logic.actions.DateDiff
import values.Variable
import values.predefined.Now

class DateDiffParser extends ActionParser {

   @Override
   public Action parse(GPathResult node, Map variables, Map params)
   {
      def name1 = node.@in1.text()
      def value1
      if (name1 == "\$now")
      {
         value1 = new Now()
      }
      else if (variables.containsKey(name1))
      {
         value1 = variables[name1]
      }
      else if (params.containsKey(name1))
      {
         value1 = params[name1]
      }
      
      
      def name2 = node.@in2.text()
      def value2
      if (name2 == "\$now")
      {
         value2 = new Now()
      }
      else if (variables.containsKey(name2))
      {
         value2 = variables[name2]
      }
      else if (params.containsKey(name2))
      {
         value2 = params[name2]
      }

      
      return new DateDiff( v1: value1, v2: value2,
                           units: node.@units.text(),
                           returnName: node.@return.text(),
                           type: 'integer') // FIXME: esto deberia ser returnType, type es 'dateDiff'
   }

}