package test;

import com.thoughtworks.xstream.XStream
import groovy.util.GroovyTestCase
import rules.Context
import rules.Registry

import parser.Parser

class BParseTest extends GroovyTestCase {

   public void testParse1()
   {
      //def path = 'C:\\Documents and Settings\\pab\\My Documents\\workspace\\rule-engine\\rules\\ejemplo1.xrl.xml'
      //def path = 'C:\\Documents and Settings\\pab\\My Documents\\workspace\\rule-engine\\rules\\rule2.xrl.xml'
      //def path = 'C:\\Documents and Settings\\pab\\My Documents\\workspace\\rule-engine\\rules\\rule3.xrl.xml'
      def path = 'C:\\Documents and Settings\\pab\\My Documents\\workspace\\rule-engine\\rules\\rule4.xrl.xml'
      def unit = new Parser().parse(new File(path))
      
      XStream xstream = new XStream()
      //String xml = xstream.toXML(unit)
      //println xml
      
      def reg = Registry.getInstance()
      reg.addUnit(unit)
      
      // hasta aqui el proceso de parsing y cache
      // ------------
      // ahora el proceso de ejecucion
      
      def clonedRule = reg.getRule(unit.id, unit.rules.collect{k,v->v}[0].id) // unit.rules es un map, me quedo con la lista de valores y pido el 0
      
      // Ver si hay diferencias entre la regla original y la clonada
      String xml = xstream.toXML(clonedRule)
      new File('clonedRule.log').write(xml)
      xml = xstream.toXML(unit.rules.collect{k,v->v}[0]) // EL ORIGINAL CAMBIA!! NO ESTA CLONANDO BIEN...
      new File('parsedRule.log').write(xml)
      
      /* Evaluacion de unit
      // Pruebo la evaluacion
      def ret = unit.evaluate('evento falso', ['par1':555, 'par2':666])
      
      // devuelve: [rule_4_1.v1:[sum1:rules.NamedValue@b32ed4]]
      // TODO: capaz habria que retornar el valor en lugar del NamedValue
      println "devuelve: " + ret
      
      // el collect se queda con la lista de NamedValue de valores del map
      // el .value pide el valor a cada NamedValue de la lista, tirando la lista de valores simples
      println "devuelve valores: " + ret['rule_4_1.v1'].collect{k,v->v}.value //.getValues().value
      */
      
      // Executer!
      // Evaluacion de rule
      def context = new Context() // Contexto de ejecucion, en el anterior se la setea unit
      clonedRule.context = context
      def ret = clonedRule.evaluate(['par1':555, 'par2':666])
      println "devuelve: " + ret
      
      
      // Debug de estructuras de reglas para ver si cambiaron luego de la ejecucion
      xml = xstream.toXML(clonedRule)
      new File('clonedRule_afterExec.log').write(xml)
      xml = xstream.toXML(unit.rules.collect{k,v->v}[0])
      new File('parsedRule_afterExec.log').write(xml)
   }
}