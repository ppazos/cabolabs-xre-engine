package values.resolution

import groovy.util.slurpersupport.NodeChild

// TODO: deberia ser todo sincronizado para que distintos threads puedan guardar y pedir info.
class HttpCache {

   /**
    * Key es request (HttpRequestData), value es response yes groovy.util.slurpersupport.NodeChild que es lo que parsea el HttpBuilder en HttpResolution.
    */
   private Map reqres = [:]
   
   // TODO: timers para dar de baja cada elemento del cache
   
   // Singleton
   private static HttpCache instance = new HttpCache()
   
   private HttpCache() {}
   
   public static HttpCache getInstance()
   {
      return instance
   }
   
   /**
    * Agrega request http y response xml al cache
    * @param req
    * @param res el XML recibido en HttpResolution
    */
   //public void add(HttpRequestData req, NodeChild res)
   public void add(String req, NodeChild res)
   {
      this.reqres[req] = res
   }
   
   //public NodeChild get(HttpRequestData req)
   public NodeChild get(String req)
   {
      //println "cache get " + this.reqres
      return this.reqres[req]
   }
}