package test;

//import com.thoughtworks.xstream.XStream
import java.util.Map;

import com.thoughtworks.xstream.XStream
import execution.Executer
import groovy.util.GroovyTestCase
import rules.Context
import rules.Registry
import rules.Rule;

import parser.Parser

class DateDiffTest extends GroovyTestCase {

   public void testParse1()
   {
      def path = 'rules\\rule_date_diff.xrl.xml'
      def unit = new Parser().parse(new File(path))
      
      XStream xstream = new XStream()
      //println xstream.toXML(unit)

      
      def reg = Registry.getInstance()
      reg.addUnit(unit)
      
      
      
      // hasta aqui el proceso de parsing y cache
      // ------------
      // ahora el proceso de ejecucion
      
      
      // Executer!
      def executer = Executer.instance
      def sessId = executer.add('unit_rule_date_diff.v1', 'rule_date_diff.v1')
      def errors
      
      try {
         errors = executer.init(sessId, ['patient_id':'550e8400-e29b-41d4-a716-446655440000'])
      } catch (Exception e) {
         e.printStackTrace()
      }
      
      // DGB
      //def rule = executer.getRule(sessId)
      //println xstream.toXML(rule)
      
      
      def results = executer.execute(sessId, [:])
      
      println "errors: " + errors
      println "devuelve: " + results.collect{k,v->v.value}
      
      errors.each { exc ->
         
         exc.printStackTrace()
      }
      
   }
}