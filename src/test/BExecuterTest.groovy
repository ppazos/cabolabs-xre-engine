package test;

import com.thoughtworks.xstream.XStream
import execution.Executer
import groovy.util.GroovyTestCase
import rules.Context
import rules.Registry
import rules.Rule;

import parser.Parser

class BExecuterTest extends GroovyTestCase {

   public void testParse1()
   {
      //def path = 'C:\\Documents and Settings\\pab\\My Documents\\workspace\\rule-engine\\rules\\ejemplo1.xrl.xml'
      //def path = 'C:\\Documents and Settings\\pab\\My Documents\\workspace\\rule-engine\\rules\\rule2.xrl.xml'
      //def path = 'C:\\Documents and Settings\\pab\\My Documents\\workspace\\rule-engine\\rules\\rule3.xrl.xml'
      def path = 'rules\\rule4.xrl.xml'
      def unit = new Parser().parse(new File(path))
      
      //XStream xstream = new XStream()
      //String xml = xstream.toXML(unit)
      //println xml
      
      def reg = Registry.getInstance()
      reg.addUnit(unit)
      
      // hasta aqui el proceso de parsing y cache
      // ------------
      // ahora el proceso de ejecucion
      
      
     
      // Parametros para distintas ejecuciones
      def params = [
         ['par1':100, 'par2':101],
         //['par1':200, 'par2':201],
         //['par1':300, 'par2':301],
         //['par1':400, 'par2':401],
         //['par1':500, 'par2':501],
         ['par1':100, 'par2':101]
      ]
      
      // Executer!
      def executer = new Executer() // TODO: singleton
      params.each { paramsi ->
         
         // Hay que clonar la regla para cada ejecucion
         def clonedRule = reg.getRule(unit.id, unit.rules.collect{k,v->v}[0].id) // unit.rules es un map, me quedo con la lista de valores y pido el 0
         def ret = executer.addAndExecute(clonedRule, paramsi)
         println "devuelve: " + ret.collect{k,v->v.value}
      }
      
   }
}