package rules

import values.NamedValueRef

/**
 * Registro de todas las unidades cargadas.
 * TODO: agregar cargador de reglas.
 * 
 * @author pab
 *
 */
@Singleton
class Registry {

   /**
    * unitId => Unit
    */
   Map units = [:]
   
   public void addUnit(Unit unit)
   {
      this.units[unit.id] = unit
   }
   
   public boolean containsUnit(String unitId)
   {
      return this.units[unitId] != null
   }
   
   // TODO: un metodo que cargue las reglas que no esten cargadas desde el repositorio.
   // la path al repo debe ser parametro del config.
   
   
   public Unit getUnit(String unitId)
   {
      return this.units[unitId]
   }
   
   /**
    * Devuelve una copia de la regla para ser ejecutada.
    * @param unitId
    * @param ruleId
    * @return
    */
   public Rule getRule(String unitId, String ruleId)
   {
      def unit = this.units[unitId]
      
      if (!unit) throw new Exception("Unit $unitId not found")
      
      def rule = unit.getRule(ruleId)
      
      if (!rule) throw new Exception("Rule $ruleId not found")
      
      def cloner = new Cloner()
      return cloner.clone(rule)
   }
   
   
   /*
   public Parameter getParameter(String unitId, String ruleId, String name)
   {
      return this.units[unitId]?.getRule(ruleId)?.getParameter(name)
   }
   
   public Variable getVariable(String unitId, String ruleId, String name)
   {
      return this.units[unitId]?.getRule(ruleId)?.getVariable(name)
   }
   
   public Parameter getParameter(NamedValueRef ref)
   {
      return this.units[ref.unitId]?.getRule(ref.ruleId)?.getParameter(ref.name)
   }
   
   public Variable getVariable(NamedValueRef ref)
   {
      return this.units[ref.unitId]?.getRule(ref.ruleId)?.getVariable(ref.name)
   }
   
   // TODO: ValueRef deberia resolver el valor internamente
   public Object getValue(NamedValueRef ref)
   {
      return this.getParameter(ref) ?: this.getVariable(ref)
   }
   */
}