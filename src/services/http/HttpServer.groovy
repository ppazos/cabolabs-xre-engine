package services.http

// http://www.redtoad.ca/ataylor/2012/02/simple-servlets-in-groovy/
//@Grab(group='org.mortbay.jetty', module='jetty-embedded', version='6.1.26')

// http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.*

//import org.mortbay.jetty.Server
//import org.mortbay.jetty.servlet.*

class HttpServer {

   /*
   def runWithJetty(servlet, port) {
      def jetty = new Server(port)
      //def context = new Context(jetty, '/', Context.SESSIONS) // jetty6
      def context = new Context(jetty, '/', ServletContextHandler.SESSIONS)
      context.addServlet(new ServletHolder(servlet), '/*')
      jetty.start()
   }
   */
   
   public static void main(String[] args) throws Exception
   {
       Server server = new Server(8080);
       server.start();
       server.join();
   }
}