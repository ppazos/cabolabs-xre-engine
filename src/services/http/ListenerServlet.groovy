package services.http

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler



// http://www.redtoad.ca/ataylor/2012/02/simple-servlets-in-groovy/
// http://vimeo.com/19146453

class ListenerServlet extends AbstractHandler {

   /*
   static void run(int port, Closure requestHandler)
   {
       // http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty


       ContextHandler context = new ContextHandler();
       context.setContextPath("/xre");
       context.setResourceBase(".");
       context.setClassLoader(Thread.currentThread().getContextClassLoader());
       server.setHandler(context);

       //context.setHandler(new HttpHandler()); // necesario? no responde el requestHandler???

       // lo tomo de jetty 6
       def servlet = new ListenerServlet(requestHandler: requestHandler)
       context.addServlet(new ServletHolder(servlet), '/*')

   }
*/
   
   public static void main(String[] args) throws Exception
   {
      def port
      if (!port) port = 8080
      Server server = new Server(port) // jetty
      
      server.setHandler(new ListenerServlet())
      
      /* http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty
       * 
       * TODO: deberia tener un servlet para el add e init y otro para el execute.
       *
       * con esto me da distintos mensajes si entro a localhost:8080/it /fr o solo /
       * ver que el "handler" es el "context" 
       *
       * ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
 
        context.addServlet(new ServletHolder(new HelloServlet()),"/*");
        context.addServlet(new ServletHolder(new HelloServlet("Buongiorno Mondo")),"/it/*");
        context.addServlet(new ServletHolder(new HelloServlet("Bonjour le Monde")),"/fr/*");
        
        
        Y el helloServlet:
        
        public class HelloServlet extends HttpServlet
        {
             private String greeting="Hello World";
             public HelloServlet(){}
             public HelloServlet(String greeting)
             {
                 this.greeting=greeting;
             }
             protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
             {
                 response.setContentType("text/html");
                 response.setStatus(HttpServletResponse.SC_OK);
                 response.getWriter().println("<h1>"+greeting+"</h1>");
                 response.getWriter().println("session=" + request.getSession(true).getId());
             }
        }
        
       */
      
      server.start();
      server.join();
   }

   @Override
   public void handle(
      String target,
      Request baseRequest,
      HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException
   {
      // se imprime 2 veces ante cualquier pedido
      println "xx" + request.parameterMap // nombre -> lista de valores
      
      // http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty
      response.setContentType("text/html;charset=utf-8");
      response.setStatus(HttpServletResponse.SC_OK);
      baseRequest.setHandled(true);
      response.getWriter().println("<h1>Hello World</h1>");
      
   }
}