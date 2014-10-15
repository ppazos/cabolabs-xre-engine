package test

import conditions.complex.AndNode
import conditions.complex.OrNode
import conditions.operators.EqualsNode
import conditions.operators.GreaterThan
import conditions.operators.MatchNode
import groovy.util.GroovyTestCase;
import values.Variable
import values.ValueType

class Conditions extends GroovyTestCase {

   void testCond1 ()
   {
      // (10.0 == 20.0 OR "hola ..." matches ".*?carlos...") AND (5 > 3)
      def cond = new AndNode(
         new OrNode(
            new EqualsNode( // da false
               new Variable( value: new Float(10.0), type: ValueType.REAL ),
               new Variable( value: new Float(20.0), type: ValueType.REAL )
            ),
            new MatchNode( // da true
               new Variable( value: "hola carlos me llamo juan" ),
               ".*?carlos.*"
            )
         ),
         new GreaterThan( // da true
            new Variable( value: new Integer(5), type: ValueType.INTEGER ),
            new Variable( value: new Integer(3), type: ValueType.INTEGER )
         )
      )
      
      if (cond.evaluate()) println "Expresion es true"
      else println "Expresion es false y no deberia..."
   }
   
   void testLazyOr ()
   {
      // 10.0 == 20.0 no se evalua porque el matches previo al OR da true
      // ("hola ..." matches ".*?carlos..." OR 10.0 == 20.0) AND (5 > 3)
      def cond = new AndNode(
         new OrNode(
            new MatchNode( // da true
               new Variable( value: "hola carlos me llamo juan" ),
               ".*?carlos.*"
            ),
            new EqualsNode( // da false
               new Variable( value: new Float(10.0), type: ValueType.REAL ),
               new Variable( value: new Float(20.0), type: ValueType.REAL )
            )
         ),
         new GreaterThan( // da true
            new Variable( value: new Integer(5), type: ValueType.INTEGER ),
            new Variable( value: new Integer(3), type: ValueType.INTEGER )
         )
      )
      
      if (cond.evaluate()) println "Expresion es true"
      else println "Expresion es false y no deberia..."
   }
}