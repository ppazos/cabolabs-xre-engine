package logic

import conditions.BooleanNode

/**
 * Bloque if-else que verifica una condicion y ejecuta bloques logicos dependiendo de su resultado.
 * if (ifCond) {
 *   ifBlock
 * } else {
 *   elseBlock
 * }
 * 
 * @author pab
 */
class IfElse extends LogicBlock {

   /**
    * Si se cumple la condicion se ejecuta el ifBlock,
    * sino se ejecuta el elseBlock.
    */
   private BooleanNode ifCond
   
   /**
    * Los bloques logicos pueden tener otros if-else y acciones.
    */
   //LogicBlock ifBlock // No tiene sentido poner un if adentro de otro if, esa condicion se resuelve usando un AND, el else si puede tener ifs adentro.
   //Action ifBlock
   //private List<Action> ifBlock // es una lista de acciones que se ejecutan secuencialmente
   private ActionBlock ifBlock // es una lista de acciones que se ejecutan secuencialmente
   
   // FIXME: con este modelo, el else solo puede tener una Accion
   // El block deberia poder ser IfElse o Lista de Acciones
   private LogicBlock elseBlock // opcional
   

   // Constructor vacio para el parser
   public IfElse()
   {
   }
   
   /**
    * if-cond
    * @param ifCond
    * @param ifBlock
    */
   public IfElse(BooleanNode ifCond, ActionBlock ifBlock)
   {
      this.ifCond = ifCond
      this.setIfBlock(ifBlock)
   }
   
   /**
    * if-cond-else
    * @param ifCond
    * @param ifBlock
    * @param elseBlock
    */
   public IfElse(BooleanNode ifCond, ActionBlock ifBlock, LogicBlock elseBlock)
   {
      this.ifCond = ifCond
      this.setIfBlock(ifBlock)
      this.setElseBlock(elseBlock)
   }
   
   
   /**
   * Necesario para implementar el clone
   */
  boolean equals(Object other)
  {
     other instanceof IfElse && this.ifCond == other.ifCond && this.ifBlock == other.ifBlock && this.elseBlock == other.elseBlock
  }
   
   
   public void setIfCond(BooleanNode cond)
   {
      this.ifCond = cond
   }
   
   public void setIfBlock(ActionBlock ifBlock)
   {
      this.ifBlock = ifBlock
      this.ifBlock.getActions().each { act ->
         act.context = context // le paso mi contexto de ejecucion
      }
   }
   
   public void setElseBlock(LogicBlock elseBlock)
   {
      this.elseBlock = elseBlock
      this.elseBlock.context = context // le paso mi contexto de ejecucion
   }

   @Override
   public Object execute()
   {
      def res
      
      println "IfElse"
      if (ifCond.evaluate())
      {
         println "   ifBlock"
         ifBlock.context = context // Le paso mi contexto al ActionBlock
         res = ifBlock.execute() // ActionBlock
      }
      else if (elseBlock) // puede no tener else...
      {
         println "   elseBlock"
         elseBlock.context = context // Le paso mi contexto de ejecucion al elseBlock
         res = elseBlock.execute() // ActionBlock o IfElse
      }
      
      return res
   }
}