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

class UnitTestMuchasVars extends GroovyTestCase {

   void testUnit1()
   {
      def start = System.currentTimeMillis()
      
      // simplemente copio las mismas vars para que las resuelva
      def vars = [
         'twitter.name': new Variable(
            name: 'twitter.name',
            type: ValueType.STRING // Por defecto es string
         ),
         'twitter.friendCount': new Variable(
            name: 'twitter.friendCount',
            type: ValueType.INTEGER
         ),
         'twitter.name1': new Variable(
            name: 'twitter.name1',
            type: ValueType.STRING // Por defecto es string
         ),
         'twitter.friendCount1': new Variable(
            name: 'twitter.friendCount1',
            type: ValueType.INTEGER
         ),
         'twitter.name2': new Variable(
            name: 'twitter.name2',
            type: ValueType.STRING // Por defecto es string
         ),
         'twitter.friendCount2': new Variable(
            name: 'twitter.friendCount2',
            type: ValueType.INTEGER
         ),
         'twitter.name3': new Variable(
            name: 'twitter.name3',
            type: ValueType.STRING // Por defecto es string
         ),
         'twitter.friendCount3': new Variable(
            name: 'twitter.friendCount3',
            type: ValueType.INTEGER
         ),
         'twitter.name4': new Variable(
            name: 'twitter.name4',
            type: ValueType.STRING // Por defecto es string
         ),
         'twitter.friendCount4': new Variable(
            name: 'twitter.friendCount4',
            type: ValueType.INTEGER
         )
      ]
      
      def resolutors = [
         'twitter.name': new HttpResolution(
            locator: 'http://api.twitter.com/1/ppazos/lists/openehr.xml',
            extractor: 'user.name' // list.user.name
         ),
         'twitter.friendCount': new HttpResolution(
            locator: 'http://api.twitter.com/1/statuses/user_timeline.xml', // ?include_entities=true&include_rts=true&screen_name=ppazos&count=2
            extractor: 'status.user.friends_count',
            aggregator: Aggregator.FIRST
         ),
         'twitter.name1': new HttpResolution(
            locator: 'http://api.twitter.com/1/ppazos/lists/openehr.xml',
            extractor: 'user.name' // list.user.name
         ),
         'twitter.friendCount1': new HttpResolution(
            locator: 'http://api.twitter.com/1/statuses/user_timeline.xml', // ?include_entities=true&include_rts=true&screen_name=ppazos&count=2
            extractor: 'status.user.friends_count',
            aggregator: Aggregator.FIRST
         ),
         'twitter.name2': new HttpResolution(
            locator: 'http://api.twitter.com/1/ppazos/lists/openehr.xml',
            extractor: 'user.name' // list.user.name
         ),
         'twitter.friendCount2': new HttpResolution(
            locator: 'http://api.twitter.com/1/statuses/user_timeline.xml', // ?include_entities=true&include_rts=true&screen_name=ppazos&count=2
            extractor: 'status.user.friends_count',
            aggregator: Aggregator.FIRST
         ),
         'twitter.name3': new HttpResolution(
            locator: 'http://api.twitter.com/1/ppazos/lists/openehr.xml',
            extractor: 'user.name' // list.user.name
         ),
         'twitter.friendCount3': new HttpResolution(
            locator: 'http://api.twitter.com/1/statuses/user_timeline.xml', // ?include_entities=true&include_rts=true&screen_name=ppazos&count=2
            extractor: 'status.user.friends_count',
            aggregator: Aggregator.FIRST
         ),
         'twitter.name4': new HttpResolution(
            locator: 'http://api.twitter.com/1/ppazos/lists/openehr.xml',
            extractor: 'user.name' // list.user.name
         ),
         'twitter.friendCount4': new HttpResolution(
            locator: 'http://api.twitter.com/1/statuses/user_timeline.xml', // ?include_entities=true&include_rts=true&screen_name=ppazos&count=2
            extractor: 'status.user.friends_count',
            aggregator: Aggregator.FIRST
         )
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
                           vars['twitter.friendCount'],
                           new Variable(value:new Integer(5), type:ValueType.INTEGER) // es una constante, no necesita nombre
                        )
                     ),
                     new ActionBlock(
                        actions: [
                           // imprime una constante 
                           new Print(
                              value: new Variable(value:'Condicion valida') // es una constante, no necesita nombre
                           ),
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
            'include_entities':true,
            'include_rts':true,
            'screen_name':'ppazos',
            'count':2
         ]
      )
      
      println res
      
      
      def now = System.currentTimeMillis()
      println now - start
   }
}