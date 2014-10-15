package rules

import conditions.complex.AndNode
import conditions.complex.NotNode
import conditions.complex.OrNode
import conditions.operators.Contains
import conditions.operators.EqualsNode
import conditions.operators.GreaterThan
import conditions.operators.MatchNode
import conditions.operators.True
import logic.ActionBlock
import logic.IfElse
import logic.actions.Action
import values.Variable
import values.NamedValueRef
import values.resolution.ActionResolution
import values.resolution.HttpResolution

class Cloner {

   def variables = [:]
   
   // Indica que la variable que se esta resolviendo es de un resolutor param
   // FIXME: solo aplica a http resolutors
   static boolean isResolutorParam = false
   
   public Object clone(Rule obj)
   {
      isResolutorParam = true
      def resolutors = clone(obj.resolutors)
      isResolutorParam = false
      
      def rule = new Rule(
         id: clone(obj.id),
         name: clone(obj.name),
         description: clone(obj.description),
         keywords: clone(obj.keywords),
         definitions: clone(obj.definitions),
         resolutors: resolutors,
         input: clone(obj.input),
         logic: clone(obj.logic))
      
      return rule
   }
   
   public Object clone(IfElse obj)
   {
      //println obj.elseBlock.class
      
      if (obj.elseBlock)
         return new IfElse(
                  ifCond: clone(obj.ifCond),
                  ifBlock: clone(obj.ifBlock),
                  elseBlock: clone(obj.elseBlock))
      
      return  new IfElse(
                ifCond: clone(obj.ifCond),
                ifBlock: clone(obj.ifBlock))
   }
   
   public Object clone(ActionBlock obj)
   {
      def actblock = new ActionBlock(
         actions: clone(obj.actions),
         returnValues: clone(obj.returnValues))
      
      return actblock
   }
   
   public Object clone(Action obj)
   {
      // El clone depende de cada accion, por eso lo debe implementar cada accion
      return obj.clone(this)
   }
   
   // ============= BOOLEAN NODES =============
   // FIXME: esto lo podria tener cada clase...
   public Object clone(AndNode obj)
   {
      def and = new AndNode(
         n1: clone(obj.n1),
         n2: clone(obj.n2))
      
      return and
   }
   public Object clone(OrNode obj)
   {
      def or = new OrNode(
         n1: clone(obj.n1),
         n2: clone(obj.n2))
      
      return or
   }
   public Object clone(NotNode obj)
   {
      def not = new NotNode(n: clone(obj.n))
      return not
   }
   
   public Object clone(EqualsNode obj)
   {
      def eq = new EqualsNode(
         v1: clone(obj.v1),
         v2: clone(obj.v2))
      return eq
   }
   public Object clone(GreaterThan obj)
   {
      def gt = new GreaterThan(
         v1: clone(obj.v1),
         v2: clone(obj.v2))
      return gt
   }
   public Object clone(MatchNode obj)
   {
      def mn = new MatchNode(
         v: clone(obj.v),
         regex: clone(obj.regex))
      return mn
   }
   public Object clone(True obj)
   {
      def mn = new True()
      return mn
   }
   public Object clone(Contains obj)
   {
      def mn = new Contains(
         collection: clone(obj.collection),
         value: clone(obj.value)
      )
      return mn
   }
   // ============= BOOLEAN NODES =============
   
   // ================= VALUES ================
   public Object clone(Variable obj)
   {
      // FIXME: si la variable es un param de un resolutor, no se deberia clonar de
      // una variable existente ni se debería poner en cache para clonaciones futuras
      // por el mismo nombre. El nombre de un param de resolutor puede colisionar
      // con el nombre de otros params de resolutors, o de variables declaradas,
      // y tenter todos distintos valores y tipos.
      
      if (!isResolutorParam)
      {
         // Hay que garantizar que las variables son las mismas en todas las acciones
         // Devuelve la variable ya clonada en lugar de clonar de nuevo con el mismo nombre
         if (variables.containsKey(obj.name))
            return variables[obj.name]
      }
      else
        println "isResolutorParam ON " + obj.name
      
      def val = new Variable(
         name: obj.name, // clone(obj.name),
         value: obj.value, // clone(obj.value), uso el valor directo porque es simple
         type: obj.type) // el type es un tipo simple, no importa crear una nueva instancia, lo mismo se podria tomar para los strings y otros tipos simples 
      
      if (!isResolutorParam)
      {
         // Solo pongo en el map si es una variable
         // sino tiene nombre es constante y no es necesario hacer referencia al mismo valor desde distintos lugares.
         if (obj.name)
         {
            variables[obj.name] = val
            //println 'clona value ' + obj.name
         }
      }
      
      return val
   }
   
   public Object clone(NamedValueRef obj)
   {
      def ref = new NamedValueRef(
         name: obj.name) // se usa el valor directo por ser string
      
      return ref
   }
   // ================= VALUES ================
   
   
   
   
   public Object clone(List obj)
   {
      def lst = []
      
      obj.each { it ->
         lst << clone(it)
      }
      
      return lst
   }
   
   public Object clone(Map obj)
   {
      def map = [:]
      
      obj.each { entry ->
         map[ clone(entry.key) ] = clone(entry.value)
      }
      
      return map
   }
   
   public Object clone(String obj)
   {
      return new String(obj)
   }
   
   // ========================================================================
   // ================= ESTOS DEBERIAN TENER SU PROPIO CLONE =================
   public Object clone(HttpResolution res)
   {
      //println "======================"
      //println res.locator
      //println res.params
      //println res.extractor
      //println res.aggregator
      //println "======================"
      
      def rsl = new HttpResolution(
         locator: res.locator, //clone(res.locator),
         params: clone(res.params),
         //xml: res.xml, //clone(res.xml), // xml es vacio en, se resuelve al ejecutar
         extractor: res.extractor, //clone(res.extractor),
         sort: res.sort,
         aggregator: res.aggregator) //clone(res.aggregator))
      
      return rsl
   }
   
   public Object clone(ActionResolution obj)
   {
      // Para este resolutor quiero que devuelva referencias a variables ya clonadas,
      // sino no puedo hacer referencia a una variable resuelta por http desde otra
      // variable resuelta por action porque crea instancias distintas de las variables,
      // entonces la referencia desde la action es una variable distinta a la resuelta por http, y no tiene valor.
      isResolutorParam = false
      def res = new ActionResolution(action: clone(obj.action))
      isResolutorParam = true
      
      return res
   }
}