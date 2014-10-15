package logic;

import logic.actions.Action


/**
 * Lista de acciones que se ejecuta secuencialmente.
 * @author pab
 *
 */
public class ActionBlock extends LogicBlock {

   private List<Action> actions = []
   
   // Los bloques de acciones pueden retornar valores
   // cuyos nombres se declaran en el XRL. Este Map
   // contiene los nombres de los valores a retornar que
   // se sacan al parsear el XRL.
   // Cuando esto se parsea es un map name=>null
   // Al terminar de ejecutar, los valores con estos nombres
   // se obtinen del contexto y se ponen en el map.
   private Map returnValues = [:]


   /**
   * Necesario para implementar el clone
   */
  boolean equals(Object other)
  {
     // FIXME: capaz tengo que ir por cada accion preguntando si es igual
     other instanceof ActionBlock && this.actions == other.actions && this.returnValues == other.returnValues
  }
   
   public void addAction(Action action)
   {
      this.actions << action
   }
   
   public List<Action> getActions()
   {
      return this.actions
   }
   
   /**
    * El parser agrega el nombre de un valor a retornar en la ejecucion.
    * @param name
    */
   public void addReturnValue(String name)
   {
      this.returnValues[name] = null // El valor se pone al ejecutar
   }
   
   @Override
   public Object execute()
   {
      def returnedValue
      this.actions.each { action ->
      
         action.context = context // Le paso mi contexto de ejecucion a cada accion
         returnedValue = action.execute()
         
         // Si la accion tiene returnName, entonces se registra el valor
         // retornado en el contexto y queda disponible para las acciones
         // que se ejecutan luego.
         if (action.returnName)
         {
            this.context.addValue(returnedValue)
         }
      }
      
      // Setea los valores que el bloque va a retornar luego de ejecutar las acciones.
      // Si algun valor no fue seteado, simplemente devuelve null para ese nombre.
      def name
      returnValues.each{ entry ->
         
         name = entry.key
         entry.value = this.context.getValue(name)
      }
      return returnValues
   }

}