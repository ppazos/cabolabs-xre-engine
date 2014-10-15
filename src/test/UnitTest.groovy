package test;

import conditions.BooleanNode
import conditions.complex.AndNode
import conditions.operators.EqualsNode
import conditions.operators.GreaterThan
import values.NamedValueRef
import groovy.util.GroovyTestCase
import logic.ActionBlock
import logic.IfElse
import logic.actions.Print
import rules.Rule
import rules.Unit
import values.Variable
import values.ValueType
import rules.Registry
import values.resolution.Aggregator
import values.resolution.HttpResolution

class UnitTest extends GroovyTestCase {

   void testUnit1()
   {
      def start = System.currentTimeMillis()
      
      
      def vars = [
         'twitter.name': new Variable(
            name: 'twitter.name',
            type: ValueType.STRING // Por defecto es string
         ),
//         'twitter.friendCount': new Variable(
//            name: 'twitter.friendCount',
//            type: ValueType.INTEGER
//         ),
         'twitter.retweeted_to_user': new Variable(
            name: 'twitter.retweeted_to_user',
            type: ValueType.INTEGER
         )
      ]
      
      def resolutors = [
         'twitter.name': new HttpResolution(
           locator: 'http://api.twitter.com/1/ppazos/lists/openehr.xml',
           extractor: 'user.name' // list.user.name
         ),
         'twitter.retweeted_to_user': new HttpResolution(
           locator: 'http://api.twitter.com/1/statuses/retweeted_to_user.xml', // ?screen_name=ppazos&count=3
           extractor: 'status.user.name', // sattuses.status.user.name
           aggregator: Aggregator.COUNT,
           // parte declarativa de los resolutors, de esto se encarga el parser
           // ver que en tiempo de ejecucion (abajo) la evaluacion pasa los valores de estos params
           params: [
              // Le cambie el tipo a List porque el nombre esta adentro
              // No se pone el tipo porque se declaran en input (ademas para hacer el request se pasa todo a string)
              // Rule.evaluate asigna los valores
              //'include_entities': new Variable(name:"include_entities", type:ValueType.BOOLEAN),
              //'include_rts': new Variable(name:"include_rts", type:ValueType.BOOLEAN),
              new Variable(name:"screen_name"),
              new Variable(name:"count")
           ]
         )
         /*
         // esta URL ahora tira Not Authorized
         'twitter.friendCount': new HttpResolution(
           locator: 'http://api.twitter.com/1/statuses/user_timeline.xml',// ?include_entities=true&include_rts=true&screen_name=ppazos&count=2
           extractor: 'status.user.friends_count',
           aggregator: Aggregator.FIRST
         )
         */
      ]
      
      def unit = new Unit(
         'unit.123.v1',
         [
            new Rule(
               id:'rule.123.v1',
               definitions: vars,
               input: [ // esto lo hace el parser
                  'include_entities': new Variable(name:'include_entities', type:ValueType.BOOLEAN),
                  'include_rts': new Variable(name:'include_rts', type:ValueType.BOOLEAN),
                  'screen_name': new Variable(name:'screen_name', type:ValueType.STRING),
                  'count': new Variable(name:'screen_name', type:ValueType.INTEGER)
               ],
               resolutors: resolutors,
               logic: 
                  new IfElse(
                     // BooleanNode
                     // (10.0 == 20.0 OR "hola ..." matches ".*?carlos...") AND (5 > 3)
                     new AndNode(
                        new EqualsNode(
                           vars['twitter.name'],
                           new Variable(value:'Pablo Pazos') // es una constante, no necesita nombre
                        ),
                        new GreaterThan(
                           //vars['twitter.friendCount'],
                           vars['twitter.retweeted_to_user'],
                           new Variable(value:new Integer(2), type:ValueType.INTEGER) // es una constante, no necesita nombre
                        )
                     ),
                     new ActionBlock(
                        actions: [
                           // imprime una constante 
                           new Print(
                              value: new Variable(value:'Condicion valida') // es una constante, no necesita nombre
                           ),
                           // Podria ponerle otro if aqui...
                           // imprime una variable existente
                           new Print(
                              value: vars['twitter.name']
                           )
                        ]
                     )
                  ) // if-else
            )
         ]
      )
      
      
      // La unit debe registrarse en el Registry para poder ser evaluada correctamente.
      // Las unidades se cargan en el registro al parsear los xmls
      def reg = Registry.getInstance()
      reg.addUnit(unit)
      
      
      /**
       * Parametros para http://api.twitter.com/1/statuses/user_timeline.xml
       * 
       * include_entities=true
       * include_rts=true
       * screen_name=ppazos
       * count=2
       */
      def res = unit.evaluate(
         'eventx',
         [  // http://api.twitter.com/1/statuses/user_timeline.xml?include_entities=true&include_rts=true&screen_name=ppazos&count=2
//            'include_entities': new Variable(name:'include_entities', value:true),
//            'include_rts': new Variable(name:'include_rts', value:true),
//            'screen_name': new Variable(name:'screen_name', value:'ppazos'),
//            'count': new Variable(name:'count', value:3)
            'include_entities': true,
            'include_rts': true,
            'screen_name': 'ppazos',
            'count': 3
         ]
      )
      
      println "result: " + res

      def now = System.currentTimeMillis()
      println "time: " + (now - start) + "ms"
   }
}