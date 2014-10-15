package logic

import rules.Context

abstract class LogicBlock implements Cloneable {

   // FIXME: el contexto no se deberia guardar adentro, deberia poder accederse desde el Executer haciendo IOC con el propio executer.
   // Contexto de ejecucion de la evaluacion de la logica
   Context context
   
  /**
   * Ejecucion de la accion o if-else, las acciones pueden devolver valor.
   */
  abstract Object execute()
}