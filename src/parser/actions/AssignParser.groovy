package parser.actions

// A veces viene NodeChild pero a veces manda NodeChildren con un elemento
// La primer superclase de ambase es GPathResult
//import groovy.util.slurpersupport.NodeChild
import groovy.util.slurpersupport.GPathResult

import logic.actions.Action
import logic.actions.Assign
import values.ValueType
import values.Variable
import values.predefined.Now

/**
 * It should declare the new variable if it's not declared yet and assigns
 * the value of another variable or a constant.
 * 
 * <action type="assign" var="new_variable" value="another_variable" returnType="date" />
 * 
 * "type" attribute should be present if the variable has not been previously declared.
 * 
 * It should support predefined variables like $now, e.g.:
 * 
 * <action type="assign" var="new_variable" value="$now" returnType="date" />
 */
class AssignParser extends ActionParser {

   @Override
   public Action parse(GPathResult node, Map variables, Map params)
   {
      println "Assign parse"
      
      def returnName = node.@var.text() // TODO: obligatorio
      def valueName  = node.@value.text() // TODO: obligatorio
      def returnType = node.@returnType.text() // opcional, string por defecto
      
      
      def value
      if (valueName == "\$now")
      {
         value = new Now()
      }
      
      // Si la variable no es now, deberia haberse declarado previamente
      
      if (variables.containsKey(valueName))
      {
         value = variables[valueName]
      }
      else if (params.containsKey(valueName))
      {
         value = params[valueName]
      }
      
      // Si no vay valor, la variable no fue declarada previamente
      if (!value)
      {
         throw new Exception("Variable '$valueName' should be declared before assigning it to another variable '$returnName'")
      }
      
      return new Assign( value: value,
                         returnName: returnName,
                         returnType: ((returnType)? new ValueType(returnType) : ValueType.STRING),
                         type: node.@type.text())
   }

}