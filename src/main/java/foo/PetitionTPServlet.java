package foo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

@WebServlet(name = "PetServletTP", urlPatterns = { "/petitionTP" })
public class PetitionTPServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		Random r = new Random();
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		// Create petition
		for (int i = 0; i < 25; i++) {
			Entity e = new Entity("Petition", "P" + i );
			int owner=r.nextInt(800);
			e.setProperty("Owner", "User"+ owner);
			e.setProperty("Date", new Date());
			e.setProperty("Body", "Contenu Petition");
			e.setProperty("Title", "Titre Petition");
			// Create random votants
			HashSet<String> fset = new HashSet<String>();
			for (int j=0;j<20;j++) {
				fset.add("U" + r.nextInt(800));
			}
			e.setProperty("votants", fset);
			e.setProperty("nbvotants", fset.size());
			datastore.put(e);
			response.getWriter().print("<li> created post:" + e.getKey() + "<br>");

		}
	}
}