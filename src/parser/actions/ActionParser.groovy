package parser.actions

import java.util.Map

// A veces viene NodeChild pero a veces manda NodeChildren con un elemento
// La primer superclase de ambase es GPathResult
//import groovy.util.slurpersupport.NodeChild
import groovy.util.slurpersupport.GPathResult

import logic.actions.Action

// FIXME: buen candidato para una interfaz...
abstract class ActionParser {

   abstract public Action parse(GPathResult node, Map variables, Map params);
}