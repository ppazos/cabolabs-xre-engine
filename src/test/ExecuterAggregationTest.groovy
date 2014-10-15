package test

import execution.Executer
import groovy.util.GroovyTestCase
import rules.Context
import rules.Registry
import rules.Rule;

import parser.Parser

class ExecuterAggregationTest extends GroovyTestCase {

   // Sin static se pierde luego del primer test
   static String sessionId
   
   // Parametros para distintas ejecuciones
//   Map params = [
//      ['command':'sum'],
//      ['command':'avg'],
//      ['command':'nonsense']
//   ]
   
   
   public void testParse1()
   {
      //def path = 'C:\\Documents and Settings\\pab\\My Documents\\workspace\\rule-engine\\rules\\ejemplo1.xrl.xml'
      def path = 'rules\\rule6.xrl.xml'
      def unit = new Parser().parse(new File(path))
      
      def reg = Registry.instance
      reg.addUnit(unit)
      
      // hasta aqui el proceso de parsing y cache
      // ------------
      // ahora el proceso de ejecucion

      
      /*
      // Executer!
      def executer = new Executer() // TODO: singleton
      params.each { paramsi ->
         
         def start = System.currentTimeMillis()
         
         // Hay que clonar la regla para cada ejecucion
         def clonedRule = reg.getRule(unit.id, unit.rules.collect{k,v->v}[0].id) // unit.rules es un map, me quedo con la lista de valores y pido el 0
         def ret = executer.addAndExecute(clonedRule, paramsi)
         println "devuelve: " + ret.collect{k,v->v.value}
         
         
         // La primer ejecucion esta en 1.6s, la segunda en 20ms
         // Los requests HTTP son caros, habria que hacerlos antes
         // de ejecutar de forma predictiva para ahorrar en las llamadas
         // que necesitan respuesta sincronica.
         def now = System.currentTimeMillis()
         println " - execution time: " + (now - start) + "ms"
      }
      */
      
      def start = System.currentTimeMillis()
      
      // Ejecucion en 3 pasos
      
      def executer = Executer.instance
      
      
      // Hay que clonar la regla para cada ejecucion
      //def clonedRule = reg.getRule(unit.id, unit.rules.collect{k,v->v}[0].id) // unit.rules es un map, me quedo con la lista de valores y pido el 0
      
      // Le puse que el add clonara la regla
      // Paso 1:
      String sessId = executer.add(unit.id, unit.rules.collect{k,v->v}[0].id)//(clonedRule)
      
      this.sessionId = sessId
      
      println "sess 1: " + this.sessionId
      
      // Paso 2: en un entorno de ejecucion se hace en seguida del paso 1
      executer.init(sessId, [:]) // no necesita parametros para resolver variables

      
      println " - execution time: " + (System.currentTimeMillis() - start) + "ms"
   }
   
   public void testExecute()
   {
      def start = System.currentTimeMillis()
      
      def executer = Executer.instance
      
      println "sess 2: " + this.sessionId
      
      // Paso 3: se ejecuta en un momento posterior pero indefinidamente luego del paso 2
      def ret = executer.execute(this.sessionId, ['command':'avg']) // necesita parametros de input
      
      
      // TODO: debe haber una limpieza de reglas que se inicializan y nunca se ejecutan
      // Un parametro de entrada podria ser por cuanto tiempo debe vivir la session de ejecucion
      // Y un valor de salida de inicializacion podria ser el TTL (asi el cliente sabe cuanto tiempo tiene)
      
      
      println "devuelve: " + ret.collect{k,v->v.value}
      
      
      println " - execution time: " + (System.currentTimeMillis() - start) + "ms"
   }
}